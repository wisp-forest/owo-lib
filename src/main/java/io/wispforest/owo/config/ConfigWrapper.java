package io.wispforest.owo.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import blue.endless.jankson.impl.POJODeserializer;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.Observable;
import io.wispforest.owo.util.ReflectionUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class ConfigWrapper<C> {

    protected final String name;
    protected final C instance;

    protected boolean loading = false;
    protected final Jankson jankson = Jankson.builder().build();

    @SuppressWarnings("rawtypes") protected final Map<String, Option> options = new LinkedHashMap<>();

    protected ConfigWrapper(Class<C> clazz) {
        ReflectionUtils.requireZeroArgsConstructor(clazz, s -> "Config model class " + s + " must provide a zero-args constructor");
        this.instance = ReflectionUtils.tryInstantiateWithNoArgs(clazz);

        var configAnnotation = instance.getClass().getAnnotation(Config.class);
        this.name = configAnnotation.name();

        try {
            this.initializeOptions(configAnnotation.saveOnModification());
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to initialize config " + this.name, e);
        }
    }

    /**
     * Save the config represented by this wrapper
     */
    public void save() {
        if (this.loading) return;
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(this.name + ".json5");

        try {
            Files.writeString(configPath, this.jankson.toJson(this.instance).toJson(JsonGrammar.JANKSON), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Owo.LOGGER.warn("Could not save config {}", this.name, e);
        }
    }

    /**
     * Load the config represented by this wrapper from
     * its associated file, or create it if it does not exist
     */
    public void load() {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(this.name + ".json5");
        if (!Files.exists(configPath)) {
            this.save();
            return;
        }

        try {
            this.loading = true;
            for (var option : this.options.values()) {
                if (Collection.class.isAssignableFrom(option.clazz())) {
                    option.backingField().setValue(null);
                }
            }

            POJODeserializer.unpackObject(this.instance, this.jankson.load(Files.readString(configPath, StandardCharsets.UTF_8)));

            for (var option : this.options.values()) {
                option.backingField().rebind(this.instance, option.key());
                option.synchronizeWithBackingField();
            }
        } catch (IOException | SyntaxError e) {
            Owo.LOGGER.warn("Could not load config {}", this.name, e);
        } finally {
            this.loading = false;
        }
    }

    public void forEachOption(Consumer<Option<?>> action) {
        for (var option : this.options.values()) {
            action.accept(option);
        }
    }

    private void initializeOptions(boolean hookSave) throws IllegalAccessException, NoSuchMethodException {
        var fields = new LinkedHashMap<String, Option.BoundField>();
        collectFieldValues("", this.instance, fields);

        for (var entry : fields.entrySet()) {
            var key = entry.getKey();
            var boundField = entry.getValue();

            var field = boundField.field();
            var fieldType = field.getType();

            Constraint constraint = null;
            if (field.isAnnotationPresent(RangeConstraint.class)) {
                var annotation = field.getAnnotation(RangeConstraint.class);

                if (NumberReflection.isNumberType(fieldType)) {
                    Predicate<?> predicate;
                    if (fieldType == long.class || fieldType == Long.class) {
                        predicate = o -> (Long) o >= annotation.min() && (Long) o <= annotation.max();
                    } else {
                        predicate = o -> ((Number) o).doubleValue() >= annotation.min() && ((Number) o).doubleValue() <= annotation.max();
                    }

                    constraint = new Constraint("Range from " + annotation.min() + " to " + annotation.max(), predicate);
                } else {
                    throw new IllegalStateException("@RangeConstraint can only be applied to numeric fields");
                }
            }

            if (field.isAnnotationPresent(RegexConstraint.class)) {
                var annotation = field.getAnnotation(RegexConstraint.class);

                if (CharSequence.class.isAssignableFrom(fieldType)) {
                    var pattern = Pattern.compile(annotation.value());
                    constraint = new Constraint("Regex " + annotation.value(), o -> pattern.matcher((CharSequence) o).matches());
                } else {
                    throw new IllegalStateException("@RegexConstraint can only be applied to fields with a string representation");
                }
            }

            if (field.isAnnotationPresent(PredicateConstraint.class)) {
                var annotation = field.getAnnotation(PredicateConstraint.class);
                var method = boundField.owner().getClass().getMethod(annotation.value(), fieldType);

                if (method.getReturnType() != boolean.class) {
                    throw new NoSuchMethodException("Return type of predicate implementation '" + annotation.value() + "' must be 'boolean'");
                }

                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalStateException("Predicated implementation '" + annotation.value() + "' must be static");
                }

                var handle = MethodHandles.publicLookup().unreflect(method);
                constraint = new Constraint("Predicate method " + annotation.value(), o -> this.invokePredicate(handle, o));
            }

            final var defaultValue = boundField.getValue();

            final var observable = Observable.of(defaultValue);
            if (hookSave) observable.observe(o -> this.save());

            this.options.put(key, new Option<>(this.name, key, defaultValue, observable, constraint, boundField));
        }
    }

    private void collectFieldValues(String prefix, Object instance, Map<String, Option.BoundField> fields) throws IllegalAccessException {
        for (var field : instance.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;

            if (field.getType().isAnnotationPresent(Nest.class)) {
                var fieldValue = field.get(instance);
                if (fieldValue != null) {
                    this.collectFieldValues(prefix + "." + field.getName(), fieldValue, fields);
                } else {
                    throw new IllegalStateException("Nested config option containers must never be null");
                }
            } else {
                fields.put(prefix + "." + field.getName(), new Option.BoundField(instance, field));
            }
        }
    }

    private boolean invokePredicate(MethodHandle predicate, Object value) {
        try {
            return (boolean) predicate.invoke(value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not invoke predicate", e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public record Constraint(String formatted, Predicate predicate) {
        public boolean test(Object value) {
            return this.predicate.test(value);
        }
    }

}
