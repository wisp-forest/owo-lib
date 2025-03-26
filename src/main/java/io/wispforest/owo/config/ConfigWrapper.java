package io.wispforest.owo.config;

import blue.endless.jankson.*;
import blue.endless.jankson.api.DeserializationException;
import blue.endless.jankson.api.SyntaxError;
import blue.endless.jankson.impl.POJODeserializer;
import blue.endless.jankson.magic.TypeMagic;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.jankson.JanksonDeserializer;
import io.wispforest.endec.format.jankson.JanksonSerializer;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.config.base.BoundedAccess;
import io.wispforest.owo.config.base.Key;
import io.wispforest.owo.config.base.SyncMode;
import io.wispforest.owo.config.options.FieldOption;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.util.Observable;
import io.wispforest.owo.util.ReflectionUtils;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    private static final Map<Identifier, ConfigWrapper<?>> KNOWN_CONFIG_INSTANCES = new LinkedHashMap<>();

    protected final Identifier id;
    protected final C instance;

    protected boolean loading = false;
    protected final Jankson jankson;

    @SuppressWarnings("rawtypes") protected final Map<Key, FieldOption> options = new LinkedHashMap<>();
    @SuppressWarnings("rawtypes") protected final Map<Key, FieldOption> optionsView = Collections.unmodifiableMap(options);

    protected final ReflectiveEndecBuilder builder;

    protected ConfigWrapper(Class<C> clazz) {
        this(clazz, (SerializationBuilder builder) -> {});
    }

    protected ConfigWrapper(Class<C> clazz, BuilderConsumer consumer) {
        this(clazz, BuilderConsumer.fullyBuild(consumer), true);

        if (KNOWN_CONFIG_INSTANCES.containsKey(this.id)) {
            throw new IllegalStateException("Config name '" + this.id + "'"
                    + " is already taken an by instance of class '" + KNOWN_CONFIG_INSTANCES.get(this.id).getClass().getName() + "'");
        } else {
            KNOWN_CONFIG_INSTANCES.put(this.id, this);
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && clazz.isAnnotationPresent(Modmenu.class)) {
            var modmenuAnnotation = clazz.getAnnotation(Modmenu.class);
            ConfigScreenProviders.<Screen, ConfigWrapper<?>>register(
                    this.id,
                    modmenuAnnotation.priorityOrder(),
                    (Class<ConfigWrapper<?>>) this.getClass(),
                    (screen, wrapper) -> ConfigScreen.createWithCustomModel(Identifier.of(modmenuAnnotation.uiModelId()), wrapper, screen)
            );
        }
    }

    protected ConfigWrapper(Class<C> clazz, Pair<Jankson, ReflectiveEndecBuilder> dataHandlers, boolean setupConfigSyncing) {
        this.jankson = dataHandlers.left();
        this.builder = dataHandlers.right();

        ReflectionUtils.requireZeroArgsConstructor(clazz, s -> "Config model class " + s + " must provide a zero-args constructor");
        this.instance = ReflectionUtils.tryInstantiateWithNoArgs(clazz);

        var configAnnotation = clazz.getAnnotation(Config.class);
        this.id = Identifier.of(configAnnotation.modId(), configAnnotation.name());

        try {
            this.initializeOptions(configAnnotation.saveOnModification());

            if (setupConfigSyncing) {
                for (var option : this.options.values()) {
                    if (option.syncMode().isNone()) continue;

                    ConfigSynchronizer.register(this);
                    break;
                }
            }
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to initialize config " + this.id, e);
        }
    }

    public static Map<Identifier, ConfigWrapper<?>> getKnownConfigInstances() {
        return Collections.unmodifiableMap(KNOWN_CONFIG_INSTANCES);
    }

    public static ConfigWrapper<?> getConfig(Identifier id) {
        var wrapper = KNOWN_CONFIG_INSTANCES.get(id);

        if (wrapper == null) {
            throw new IllegalStateException("Unable to locate the given wrapper instance with the following id: " + id);
        }

        return wrapper;
    }

    public static Map<String, Map<String, ConfigWrapper<?>>> getGroupedConfigInstances() {
        Map<String, Map<String, ConfigWrapper<?>>> baseMap = new HashMap<>();

        for (var entry : KNOWN_CONFIG_INSTANCES.entrySet()) {
            var configId = entry.getKey();
            var wrapper = entry.getValue();

            baseMap.computeIfAbsent(configId.getNamespace(), string -> new LinkedHashMap<>())
                    .put(configId.getPath(), wrapper);
        }

        return baseMap;
    }

    /**
     * Save the config represented by this wrapper
     */
    public void saveToFile() {
        if (this.loading) return;

        try {
            this.fileLocation().getParent().toFile().mkdirs();
            Files.writeString(this.fileLocation(), saveToObject().toJson(JsonGrammar.JANKSON), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Owo.LOGGER.warn("Could not save config {}", this.id, e);
        }
    }

    public JsonObject saveToObject() {
        return (JsonObject) this.jankson.toJson(this.instance);
    }

    public void reload() {
        if (memoryData == null) {
            loadFile();
        } else {
            load(memoryData, false);
        }
    }

    public void loadFile() {
        if (!Files.exists(this.fileLocation())) {
            this.saveToFile();
            return;
        }

        try {
            var configObject = this.jankson.load(Files.readString(this.fileLocation(), StandardCharsets.UTF_8));

            load(configObject, true);
        } catch (IOException | SyntaxError e) {
            Owo.LOGGER.warn("Could not load config {}", this.id, e);
        } finally {
            this.loading = false;
        }
    }

    /**
     * Load the config represented by this wrapper from
     * its associated file, or create it if it does not exist
     */
    @SuppressWarnings({"unchecked"})
    public boolean load(JsonObject configObject, boolean allowServerSync) {
        try {
            this.loading = true;

            for (var option : this.options.values()) {
                Object newValue;

                final var clazz = option.clazz();
                final var element = configObject.recursiveGet(JsonElement.class, option.key().asString());
                if (element == null) {
                    option.set(option.defaultValue());
                    continue;
                }

                if (Map.class.isAssignableFrom(clazz)) {
                    var genericType = option.getGenericType();

                    newValue = TypeMagic.createAndCast(clazz);
                    POJODeserializer.unpackMap(
                            (Map<Object, Object>) newValue,
                            ReflectionUtils.getTypeArgument(genericType, 0),
                            ReflectionUtils.getTypeArgument(genericType, 1),
                            element,
                            this.jankson.getMarshaller()
                    );
                } else if (List.class.isAssignableFrom(clazz) || Set.class.isAssignableFrom(clazz)) {
                    newValue = TypeMagic.createAndCast(clazz);
                    POJODeserializer.unpackCollection(
                            (Collection<Object>) newValue,
                            ReflectionUtils.getTypeArgument(option.getGenericType(), 0),
                            element,
                            this.jankson.getMarshaller()
                    );
                } else {
                    newValue = configObject.getMarshaller().marshall(clazz, element);
                }

                if (!option.verifyConstraint(newValue)) continue;

                option.set(newValue == null ? option.defaultValue() : newValue);
            }

            var server = Owo.currentServer();

            if (server != null && allowServerSync) {
                for (var player : server.getPlayerManager().getPlayerList()) {
                    ConfigSynchronizer.sendLoadedServerConfig(player.networkHandler::sendPacket, this.id);
                }
            }

            return true;
        } catch (DeserializationException e) {
            Owo.LOGGER.warn("Could not load config {}", this.id, e);

            return false;
        }
    }

    /**
     * Query the field associated with a given key. This is relevant
     * in cases where said field is annotated with {@link Nest}, meaning
     * that {@link #optionForKey(Key)} would return {@code null}
     * because the field won't be treated as an option in itself.
     *
     * @param key The for which to query the field
     * @return The field described by {@code key}, or {@code null}
     * if it does not point to a valid field in the config tree
     */
    public @Nullable Field fieldForKey(Key key) {
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

    public Identifier id() {
        return this.id;
    }

    /**
     * @return The name of this config, used for translation
     * keys and the filename
     */
    public String name() {
        return this.id.getPath();
    }

    /**
     * @return The location to which this config is saved
     */
    public Path fileLocation() {
        return FabricLoader.getInstance().getConfigDir().resolve(this.name() + ".json5");
    }

    /**
     * Query the config option associated with a given key
     *
     * @param key The key for which to query the option
     * @return The option described by {@code key}, or {@code null}
     * if no such option exists
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable FieldOption<T> optionForKey(Key key) {
        return this.options.get(key);
    }

    /**
     * @return A view of all options contained in this config
     */
    @SuppressWarnings("unchecked")
    public Map<Key, FieldOption<?>> allOptions() {
        return (Map<Key, FieldOption<?>>) (Object) this.optionsView;
    }

    /**
     * Execute the given action once for each option in this config
     */
    public void forEachOption(Consumer<FieldOption<?>> action) {
        for (var option : this.options.values()) {
            action.accept(option);
        }
    }

    private void initializeOptions(boolean hookSave) throws IllegalAccessException, NoSuchMethodException {
        var fields = new LinkedHashMap<Key, BoundedAccess.BoundField<Object>>();
        collectFieldValues(Key.ROOT, this.instance, fields);

        var instanceSyncMode = this.instance.getClass().isAnnotationPresent(Sync.class)
                ? this.instance.getClass().getAnnotation(Sync.class).value()
                : SyncMode.NONE;

        for (var entry : fields.entrySet()) {
            var key = entry.getKey();
            var boundField = entry.getValue();

            var field = boundField.field();

            var constraint = ConfigReflectionUtils.getConstraint(boundField);

            final var defaultValue = boundField.getValue();

            final var observable = Observable.of(defaultValue);
            if (hookSave) observable.observe(o -> this.saveToFile());

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

            this.options.put(key, new FieldOption<>(this.id(), key, defaultValue, observable, boundField, constraint, syncMode, this.builder));
        }
    }

    private void collectFieldValues(Key parent, Object instance, Map<Key, BoundedAccess.BoundField<Object>> fields) throws IllegalAccessException {
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
                fields.put(parent.child(field.getName()), new BoundedAccess.BoundField<>(instance, field));
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

    public record SerializationBuilder(Jankson.Builder janksonBuilder, ReflectiveEndecBuilder endecBuilder) {
        public <T> SerializationBuilder addEndec(Class<T> clazz, Endec<T> endec) {
            endecBuilder().register(endec, clazz);

            janksonBuilder()
                    .registerSerializer(clazz, (t, marshaller) -> endec.encodeFully(JanksonSerializer::of, t))
                    .registerDeserializer(JsonElement.class, clazz, (element, marshaller) -> endec.decodeFully(JanksonDeserializer::of, element));

            return this;
        }
    }

    public interface BuilderConsumer {
        void build(SerializationBuilder builder);

        static Pair<Jankson, ReflectiveEndecBuilder> fullyBuild(BuilderConsumer consumer) {
            var builder = new SerializationBuilder(Jankson.builder(), MinecraftEndecs.addDefaults(new ReflectiveEndecBuilder()));

            builder.janksonBuilder()
                    .registerSerializer(Identifier.class, (identifier, marshaller) -> new JsonPrimitive(identifier.toString()))
                    .registerDeserializer(JsonPrimitive.class, Identifier.class, (primitive, m) -> Identifier.tryParse(primitive.asString()));

            builder.addEndec(Color.class, Color.RGBA_HEX_ENDEC);

            consumer.build(builder);

            return Pair.of(builder.janksonBuilder().build(), builder.endecBuilder());
        }
    }

    public static ConfigWrapper<?> getOrDuplicateWrapper(Identifier configId, @Nullable JsonObject jsonObject) {
        var wrapper = ConfigWrapper.getKnownConfigInstances().get(configId);

        if (wrapper == null) {
            throw new IllegalStateException("Unable to locate the given wrapper instance with the following id: " + configId);
        }

        if (jsonObject != null) {
            wrapper = wrapper.attemptToDuplicate(jsonObject);
        }

        return wrapper;
    }

    //--

    private boolean serverConfig = false;

    public boolean isServerConfig() {
        return this.serverConfig;
    }

    @Nullable
    private JsonObject memoryData = null;

    @ApiStatus.Internal
    @Nullable
    private ConfigWrapper<?> attemptToDuplicate(JsonObject jsonObject) {
        var clazz = this.getClass();

        try {
            var constructor = clazz.getDeclaredConstructor(Class.class, Pair.class, boolean.class);

            if (constructor.trySetAccessible()) {
                var newWrapper = constructor.newInstance(this.instance.getClass(), Pair.of(this.jankson, this.builder), false);

                if (newWrapper.load(jsonObject, false)) {
                    newWrapper.serverConfig = true;
                    newWrapper.memoryData = jsonObject;

                    return newWrapper;
                } else {
                    Owo.LOGGER.warn("Could not load the given duplicated config {}", this.id);
                }
            } else {
                Owo.LOGGER.warn("Could not construct the given duplicated config {} due to construct being in accessible", this.id);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Owo.LOGGER.warn("Could not duplicate the given config {}", this.id, e);
        }

        return null;
    }
}
