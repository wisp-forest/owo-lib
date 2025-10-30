package io.wispforest.owo.config;

import blue.endless.jankson.Comment;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.CommentAttribute;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.config.base.BoundedAccess;
import io.wispforest.owo.config.base.Key;
import io.wispforest.owo.config.base.SyncMode;
import io.wispforest.owo.config.options.FieldOption;
import io.wispforest.owo.config.serialization.ConfigSerializer;
import io.wispforest.owo.config.serialization.RawConfigData;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.util.Observable;
import io.wispforest.owo.util.ReflectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

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

    protected final ConfigSerializer<?> serializer;

    protected final Identifier id;
    protected final C instance;
    protected final Endec<C> instanceEndec;

    protected boolean loading = false;

    @SuppressWarnings("rawtypes") protected final Map<Key, FieldOption> options = new LinkedHashMap<>();
    @SuppressWarnings("rawtypes") protected final Map<Key, FieldOption> optionsView = Collections.unmodifiableMap(options);

    protected final ReflectiveEndecBuilder builder;

    protected ConfigWrapper(Class<C> clazz) {
        this(clazz, ConfigSerializer.JANKSON);
    }

    protected ConfigWrapper(Class<C> clazz, ConfigSerializer<?> serializer) {
        this(clazz, serializer, builder1 -> {});
    }

    protected ConfigWrapper(Class<C> clazz, Builder consumer) {
        this(clazz, ConfigSerializer.JANKSON, consumer);
    }

    protected ConfigWrapper(Class<C> clazz, ConfigSerializer<?> serializer, Builder consumer) {
        this(clazz, serializer, Builder.fullyBuild(consumer), true);

        if (KNOWN_CONFIG_INSTANCES.containsKey(this.id)) {
            throw new IllegalStateException("Config name '" + this.id + "'"
                    + " is already taken by an instance of class '" + KNOWN_CONFIG_INSTANCES.get(this.id).getClass().getName() + "'");
        } else {
            KNOWN_CONFIG_INSTANCES.put(this.id, this);
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && clazz.isAnnotationPresent(Modmenu.class)) {
            var modmenuAnnotation = clazz.getAnnotation(Modmenu.class);

            ConfigScreenProviders.register(this, modmenuAnnotation.priorityOrder(), Identifier.of(modmenuAnnotation.uiModelId()));
        }
    }

    protected ConfigWrapper(Class<C> clazz, ConfigSerializer<?> serializer, ReflectiveEndecBuilder builder, boolean setupConfigSyncing) {
        var configAnnotation = clazz.getAnnotation(Config.class);
        this.id = Identifier.of(configAnnotation.modId(), configAnnotation.name());

        this.serializer = serializer;

        this.builder = builder;

        ReflectionUtils.requireZeroArgsConstructor(clazz, s -> "Config model class " + s + " must provide a zero-args constructor");
        this.instance = ReflectionUtils.tryInstantiateWithNoArgs(clazz);

        try {
            this.instanceEndec = builder.get(clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create the required Endec for '" + id + "' due to an error: ", e);
        }

        try {
            this.initializeOptions(configAnnotation.saveOnModification());

            if (setupConfigSyncing) {
                boolean shouldRegisterForSync = false;

                for (var option : this.options.values()) {
                    if (option.syncMode().isNone()) continue;

                    shouldRegisterForSync = true;

                    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) break;

                    if (option.syncMode() == SyncMode.INFORM_SERVER) {
                        // TODO: FIND A BETTER WAY FOR IF MULTIPLE VALUES ARE CHANGED AT ONCE IT DOSE NOT TRY TO SYNC EACH TIME?
                        option.observe(object -> ConfigSynchronizer.sendChangedConfigValues(this.id()));
                    }
                }

                if (shouldRegisterForSync) ConfigSynchronizer.register(this);
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

    public static Map<String, SequencedMap<String, ConfigWrapper<?>>> getGroupedConfigInstances() {
        var baseMap = new HashMap<String, SequencedMap<String, ConfigWrapper<?>>>();

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

            Files.writeString(this.fileLocation(), serializer.encodeToString(SerializationContext.empty(), this.instanceEndec, this.instance), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Owo.LOGGER.warn("Could not save config {}", this.id, e);
        }
    }

    public RawConfigData<?> saveToRawData() {
        return serializer.encodeToRaw(SerializationContext.empty(), this.instanceEndec, this.instance);
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
            load(serializer.decodeToRaw(Files.readString(this.fileLocation(), StandardCharsets.UTF_8)), true);
        } catch (Exception e) {
            Owo.LOGGER.warn("Could not read config file {}", this.id, e);
        }
    }

    /**
     * Load the config represented by this wrapper from
     * its associated file, or create it if it does not exist
     */
    public <E> boolean load(RawConfigData<E> holder, boolean allowServerSync) {
        return load(holder.element(), holder.serializer(), allowServerSync);
    }

    @SuppressWarnings({"unchecked"})
    public <E> boolean load(E configObject, ConfigSerializer<E> serializer, boolean allowServerSync) {
        try {
            this.loading = true;

            for (var option : this.options.values()) {
                final var element = serializer.getElementForKey(configObject, option.key());

                if (element == null) {
                    option.set(option.defaultValue());
                    continue;
                }

                final var newValue = serializer.decodeFromFormat(SerializationContext.empty(), option.endec(), element);

                if (!option.verifyConstraint(newValue)) continue;

                option.set(newValue == null ? option.defaultValue() : newValue);
            }

            if (allowServerSync) {
                ConfigSynchronizer.sendLoadedServerConfig(this.id);
            }

            return true;
        } catch (Exception e) {
            Owo.LOGGER.warn("Could not load config {}", this.id, e);

            return false;
        } finally {
            this.loading = false;
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
                clazz = clazz.getDeclaredField(path.removeFirst()).getType();
            }

            return clazz.getField(path.getFirst());
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
            // TODO: FIND A BETTER WAY FOR IF MULTIPLE VALUES ARE CHANGED AT ONCE IT DOSE NOT TRY TO SAVE EACH TIME?
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
                if (fieldValue == null) {
                    throw new IllegalStateException("Nested config option containers must never be null");
                }

                this.collectFieldValues(parent.child(field.getName()), fieldValue, fields);
            } else {
                fields.put(parent.child(field.getName()), new BoundedAccess.BoundField<>(instance, field));
            }
        }
    }

    public interface Builder {
        void build(ReflectiveEndecBuilder builder);

        static ReflectiveEndecBuilder fullyBuild(Builder consumer) {
            var builder = MinecraftEndecs.addDefaults(new ReflectiveEndecBuilder())
                .register(Color.RGBA_HEX_ENDEC, Color.class);

            // TODO: REMOVE WITHIN THE FUTURE
            builder.registerContextGatherer(Comment.class, (annotatedType, annotation) -> {
                return SerializationContext.attributes(new CommentAttribute(annotation.value()));
            });

            consumer.build(builder);

            return builder;
        }
    }

    public static ConfigWrapper<?> getOrDuplicateWrapper(Identifier configId, @Nullable RawConfigData<?> data) {
        var wrapper = ConfigWrapper.getKnownConfigInstances().get(configId);

        if (wrapper == null) {
            throw new IllegalStateException("Unable to locate the given wrapper instance with the following id: " + configId);
        }

        if (data != null) {
            wrapper = wrapper.attemptToDuplicate(data);
        }

        return wrapper;
    }

    //--

    private boolean serverConfig = false;

    public boolean isServerConfig() {
        return this.serverConfig;
    }

    @Nullable
    private RawConfigData<?> memoryData = null;

    @ApiStatus.Internal
    @Nullable
    private ConfigWrapper<?> attemptToDuplicate(RawConfigData<?> data) {
        if (!data.isFrom(this.serializer)) {
            Owo.LOGGER.warn("Could not load data into the given duplicated config {} as the serializer of the data do not match", this.id);
        }

        var clazz = this.getClass();

        try {
            var constructor = clazz.getDeclaredConstructor(Class.class, ReflectiveEndecBuilder.class, boolean.class);

            if (constructor.trySetAccessible()) {
                var newWrapper = constructor.newInstance(this.instance.getClass(), this.builder, false);

                if (newWrapper.load(data, false)) {
                    newWrapper.serverConfig = true;
                    newWrapper.memoryData = data;

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
