package io.wispforest.owo.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.SyntaxError;
import blue.endless.jankson.impl.POJODeserializer;
import blue.endless.jankson.magic.TypeMagic;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.Observable;
import io.wispforest.owo.util.ReflectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The common base class of all generated config classes.
 * The majority of all config functionality resides in here
 * <p>
 * Do not extend this class yourself - instead annotate
 * a class describing your config model with {@link Config},
 * just as you would do with other libraries like Cloth Config
 *
 * @see Config
 */
public abstract class ConfigWrapper<C> {

    @Environment(EnvType.CLIENT)
    private static final Map<String, Function<Screen, ConfigScreen>> CONFIG_SCREEN_PROVIDERS = new HashMap<>();

    protected final String name;
    protected final C instance;

    protected boolean loading = false;
    protected final Jankson jankson = Jankson.builder().build();

    @SuppressWarnings("rawtypes") protected final Map<Option.Key, Option> options = new LinkedHashMap<>();
    @SuppressWarnings("rawtypes") protected final Map<Option.Key, Option> optionsView = Collections.unmodifiableMap(options);

    protected ConfigWrapper(Class<C> clazz) {
        ReflectionUtils.requireZeroArgsConstructor(clazz, s -> "Config model class " + s + " must provide a zero-args constructor");
        this.instance = ReflectionUtils.tryInstantiateWithNoArgs(clazz);

        var configAnnotation = clazz.getAnnotation(Config.class);
        this.name = configAnnotation.name();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && clazz.isAnnotationPresent(Modmenu.class)) {
            var modmenuAnnotation = clazz.getAnnotation(Modmenu.class);
            CONFIG_SCREEN_PROVIDERS.put(
                    modmenuAnnotation.modId(),
                    screen -> ConfigScreen.createWithCustomModel(new Identifier(modmenuAnnotation.uiModelId()), this, screen)
            );
        }

        try {
            this.initializeOptions(configAnnotation.saveOnModification());
            for (var option : this.options.values()) {
                if (option.syncMode().isNone()) continue;

                ConfigSynchronizer.register(this);
                break;
            }
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to initialize config " + this.name, e);
        }
    }

    /**
     * @return The name of this config, used for translation
     * keys and the filename
     */
    public String name() {
        return this.name;
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
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void load() {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(this.name + ".json5");
        if (!Files.exists(configPath)) {
            this.save();
            return;
        }

        try {
            this.loading = true;
            var configObject = this.jankson.load(Files.readString(configPath, StandardCharsets.UTF_8));

            for (var option : this.options.values()) {
                Object newValue;

                final var clazz = option.clazz();
                if (Map.class.isAssignableFrom(clazz)) {
                    var field = option.backingField().field();

                    newValue = TypeMagic.createAndCast(clazz);
                    POJODeserializer.unpackMap(
                            (Map<Object, Object>) newValue,
                            ReflectionUtils.getTypeArgument(field.getGenericType(), 0),
                            ReflectionUtils.getTypeArgument(field.getGenericType(), 1),
                            configObject.recursiveGet(JsonElement.class, option.key().asString()),
                            this.jankson.getMarshaller()
                    );
                } else if (List.class.isAssignableFrom(clazz)) {
                    newValue = TypeMagic.createAndCast(clazz);
                    POJODeserializer.unpackCollection(
                            (Collection<Object>) newValue,
                            ReflectionUtils.getTypeArgument(option.backingField().field().getGenericType(), 0),
                            configObject.recursiveGet(JsonElement.class, option.key().asString()),
                            this.jankson.getMarshaller()
                    );
                } else {
                    newValue = configObject.recursiveGet(clazz, option.key().asString());
                }

                if (!option.verifyConstraint(newValue)) continue;

                option.set(newValue == null ? option.defaultValue() : newValue);
            }
        } catch (IOException | SyntaxError | DeserializationException e) {
            Owo.LOGGER.warn("Could not load config {}", this.name, e);
        } finally {
            this.loading = false;
        }
    }

    /**
     * Query the field associated with a given key. This is relevant
     * in cases where said field is annotated with {@link Nest}, meaning
     * that {@link #optionForKey(Option.Key)} would return {@code null}
     * because the field won't be treated as an option in itself.
     *
     * @param key The for which to query the field
     * @return The field described by {@code key}, or {@code null}
     * if it does not point to a valid field in the config tree
     */
    public @Nullable Field fieldForKey(Option.Key key) {
        try {
            var path = new ArrayList<>(List.of(key.path()));
            var clazz = this.instance.getClass();

            while (path.size() > 1) {
                clazz = clazz.getDeclaredField(path.remove(0)).getType();
            }

            return clazz.getField(path.get(0));
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Query the config option associated with a given key
     *
     * @param key The key for which to query the option
     * @return The option described by {@code key}, or {@code null}
     * if no such option exists
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable Option<T> optionForKey(Option.Key key) {
        return this.options.get(key);
    }

    /**
     * @return A view of all options contained in this config
     */
    @SuppressWarnings("unchecked")
    public Map<Option.Key, Option<?>> allOptions() {
        return (Map<Option.Key, Option<?>>) (Object) this.optionsView;
    }

    /**
     * Execute the given action once for each option in this config
     */
    public void forEachOption(Consumer<Option<?>> action) {
        for (var option : this.options.values()) {
            action.accept(option);
        }
    }

    private void initializeOptions(boolean hookSave) throws IllegalAccessException, NoSuchMethodException {
        var fields = new LinkedHashMap<Option.Key, Option.BoundField<Object>>();
        collectFieldValues(Option.Key.ROOT, this.instance, fields);

        var instanceSyncMode = this.instance.getClass().isAnnotationPresent(Sync.class)
                ? this.instance.getClass().getAnnotation(Sync.class).value()
                : Option.SyncMode.NONE;

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

            var syncMode = instanceSyncMode;
            if (field.isAnnotationPresent(Sync.class)) {
                syncMode = field.getAnnotation(Sync.class).value();
            } else {
                var parentKey = key.parent();
                while (!parentKey.isRoot()) {
                    var parentField = this.fieldForKey(parentKey);
                    if (parentField.isAnnotationPresent(Sync.class)) {
                        syncMode = parentField.getAnnotation(Sync.class).value();
                    }

                    parentKey = parentKey.parent();
                }
            }

            this.options.put(key, new Option<>(this.name, key, defaultValue, observable, boundField, constraint, syncMode));
        }
    }

    private void collectFieldValues(Option.Key parent, Object instance, Map<Option.Key, Option.BoundField<Object>> fields) throws IllegalAccessException {
        for (var field : instance.getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;

            if (field.isAnnotationPresent(Nest.class)) {
                var fieldValue = field.get(instance);
                if (fieldValue != null) {
                    this.collectFieldValues(parent.child(field.getName()), fieldValue, fields);
                } else {
                    throw new IllegalStateException("Nested config option containers must never be null");
                }
            } else {
                fields.put(parent.child(field.getName()), new Option.BoundField<>(instance, field));
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

    public static void forEachScreenProvider(BiConsumer<String, Function<Screen, ConfigScreen>> action) {
        CONFIG_SCREEN_PROVIDERS.forEach(action);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public record Constraint(String formatted, Predicate predicate) {
        public boolean test(Object value) {
            return this.predicate.test(value);
        }
    }

}
