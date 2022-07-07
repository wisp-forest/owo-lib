package io.wispforest.owo.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import blue.endless.jankson.impl.POJODeserializer;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.util.ReflectionUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class ConfigWrapper<C> {

    protected final String filename;
    protected C instance;

    protected boolean loading = false;

    protected final Jankson jankson = Jankson.builder().build();
    protected final Map<String, Constraint> constraints = new HashMap<>();
    @SuppressWarnings("rawtypes") protected final Map<String, Observable> listeners = new HashMap<>();

    protected ConfigWrapper(Class<C> clazz) {
        ReflectionUtils.requireZeroArgsConstructor(clazz, s -> "Config model class " + s + " must provide a zero-args constructor");
        this.instance = ReflectionUtils.tryInstantiateWithNoArgs(clazz);

        var configAnnotation = instance.getClass().getAnnotation(Config.class);
        this.filename = configAnnotation.name();

        try {
            this.initialize(configAnnotation.saveOnModification());
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to initialize config " + this.filename, e);
        }
    }

    /**
     * Save the config represented by this wrapper
     */
    public void save() {
        if (this.loading) return;
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(this.filename + ".json5");

        try {
            Files.writeString(configPath, this.jankson.toJson(this.instance).toJson(JsonGrammar.JANKSON), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Owo.LOGGER.warn("Could not save config {}", this.filename, e);
        }
    }

    /**
     * Load the config represented by this wrapper from
     * its associated file, or create it if it does not exist
     */
    @SuppressWarnings("unchecked")
    public void load() {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(this.filename + ".json5");
        if (!Files.exists(configPath)) {
            this.save();
            return;
        }

        try {
            this.loading = true;

            this.instance = this.newInstanceWithNullCollections();
            POJODeserializer.unpackObject(this.instance, this.jankson.load(Files.readString(configPath, StandardCharsets.UTF_8)));

            var newValues = new HashMap<String, BoundField>();
            this.collectFieldValues("", this.instance, newValues);

            for (var entry : newValues.entrySet()) {
                var key = entry.getKey();
                var boundField = entry.getValue();

                if (!this.verifyConstraint(key, boundField.value)) {
                    boundField.field.set(boundField.owner, this.listeners.get(key).get());
                } else {
                    this.listeners.get(key).set(boundField.value);
                }
            }
        } catch (IOException | SyntaxError | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            Owo.LOGGER.warn("Could not load config {}", this.filename, e);
        } finally {
            this.loading = false;
        }
    }

    protected boolean verifyConstraint(String key, Object value) {
        var constraint = this.constraints.get(key);
        if (constraint == null) return true;

        final var matched = constraint.test(value);
        if (!matched) {
            Owo.LOGGER.warn(
                    "Option {} in config '{}' could not be updated, as the given value '{}' does not match its constraint: {}",
                    key, this.filename, value, constraint.formatted
            );
        }

        return matched;
    }

    private void initialize(boolean hookSave) throws IllegalAccessException, NoSuchMethodException {
        var fields = new HashMap<String, BoundField>();
        collectFieldValues("", this.instance, fields);

        for (var entry : fields.entrySet()) {
            var key = entry.getKey();
            var boundField = entry.getValue();

            var field = boundField.field;

            if (field.isAnnotationPresent(RangeConstraint.class)) {
                var constraint = field.getAnnotation(RangeConstraint.class);

                if (Number.class.isAssignableFrom(field.getType())
                        || field.getType().isPrimitive() && field.getType() != boolean.class) {

                    Predicate<?> predicate;
                    if (field.getType() == long.class || field.getType() == Long.class) {
                        predicate = o -> (Long) o >= constraint.min() && (Long) o <= constraint.max();
                    } else if (field.getType() == char.class) {
                        predicate = o -> (Character) o >= constraint.min() && (Character) o <= constraint.max();
                    } else {
                        predicate = o -> ((Number) o).doubleValue() >= constraint.min() && ((Number) o).doubleValue() <= constraint.max();
                    }

                    this.constraints.put(key, new Constraint("Range from " + constraint.min() + " to " + constraint.max(), predicate));
                } else {
                    throw new IllegalStateException("@RangeConstraint can only be applied to numeric fields");
                }
            }

            if (field.isAnnotationPresent(RegexConstraint.class)) {
                var constraint = field.getAnnotation(RegexConstraint.class);

                if (CharSequence.class.isAssignableFrom(field.getType())) {
                    var pattern = Pattern.compile(constraint.value());
                    this.constraints.put(key, new Constraint("Regex " + constraint.value(), o -> pattern.matcher((CharSequence) o).matches()));
                } else {
                    throw new IllegalStateException("@RegexConstraint can only be applied to fields with a string representation");
                }
            }

            if (field.isAnnotationPresent(PredicateConstraint.class)) {
                var constraint = field.getAnnotation(PredicateConstraint.class);
                var method = boundField.owner.getClass().getMethod(constraint.value(), field.getType());

                if (method.getReturnType() != boolean.class) {
                    throw new NoSuchMethodException("Return type of predicate implementation '" + constraint.value() + "' must be 'boolean'");
                }

                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalStateException("Predicated implementation '" + constraint.value() + "' must be static");
                }

                var handle = MethodHandles.publicLookup().unreflect(method);
                this.constraints.put(key, new Constraint("Predicate method " + constraint.value(), o -> this.invokePredicate(handle, o)));
            }

            final var observable = new Observable<>(boundField.value);
            if (hookSave) observable.observe(o -> this.save());

            this.listeners.put(key, observable);
        }
    }

    private void collectFieldValues(String prefix, Object instance, Map<String, BoundField> fields) throws IllegalAccessException {
        if (instance == null) return;
        var clazz = instance.getClass();

        for (var field : clazz.getDeclaredFields()) {
            fields.put(prefix + "." + field.getName(), new BoundField(instance, field, field.get(instance)));

            if (field.getType().isAnnotationPresent(Nest.class)) {
                this.collectFieldValues(prefix + "." + field.getName(), field.get(instance), fields);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private C newInstanceWithNullCollections() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var clazz = (Class<C>) this.instance.getClass();

        final var instance = ReflectionUtils.tryInstantiateWithNoArgs(clazz);
        for (var field : clazz.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
            if (!Collection.class.isAssignableFrom(field.getType())) continue;
            field.set(instance, null);
        }
        return instance;
    }

    private boolean invokePredicate(MethodHandle predicate, Object value) {
        try {
            return (boolean) predicate.invoke(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private record Constraint(String formatted, Predicate predicate) {
        public boolean test(Object value) {
            return this.predicate.test(value);
        }
    }

    private record BoundField(Object owner, Field field, Object value) {}
}
