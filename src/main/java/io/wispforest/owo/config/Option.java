package io.wispforest.owo.config;

import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.annotation.RestartRequired;
import io.wispforest.endec.Endec;
import io.wispforest.owo.util.Observable;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Describes a single option in a config. Instances
 * of this class keep a reference to the field in
 * the model class which stores the value used for serialization.
 * <p>
 * An option may enter the so-called "detached" state, which means
 * its value is being overridden by the server. In this state, the option
 * is completely immutable and can only be changed again afterwards
 */
public final class Option<T> {

    private final String configName;
    private final Key key;
    private final String translationKey;

    private final T defaultValue;
    private final Observable<T> mirror;

    private final BoundField<T> backingField;
    private final Class<T> clazz;

    private final ConfigWrapper.@Nullable Constraint constraint;
    private final @Nullable Endec<T> endec;
    private final SyncMode syncMode;

    /**
     * Indicates whether this option is currently being overridden
     * by the server and should thus never synchronize with its backing
     * field and behave immutably to the client
     */
    private boolean detached = false;

    /**
     * @param configName   The name of the config this option is contained in
     * @param key          The key of this option
     * @param defaultValue The default value of this option
     * @param mirror       A mirror of the value of this option, used for
     *                     emitting events when it changes as well as correcting
     *                     invalid values after deserialization
     * @param backingField The backing field in the config model class
     *                     which this option describes
     * @param constraint   The constraint placed on the value of this option,
     *                     or {@code null} if the option is unconstrained
     */
    @SuppressWarnings("unchecked")
    public Option(String configName,
                  Key key,
                  T defaultValue,
                  Observable<T> mirror,
                  BoundField<T> backingField,
                  @Nullable ConfigWrapper.Constraint constraint,
                  SyncMode syncMode,
                  ReflectiveEndecBuilder builder
    ) {
        this.configName = configName;
        this.key = key;
        this.translationKey = "text.config." + this.configName + ".option." + this.key.asString();

        this.defaultValue = defaultValue;
        this.mirror = mirror;

        this.backingField = backingField;
        this.clazz = (Class<T>) backingField.field().getType();

        this.constraint = constraint;
        this.syncMode = syncMode;
        this.endec = syncMode.isNone() ? null : (Endec<T>) builder.get(this.backingField.field.getGenericType());
    }

    /**
     * Update the current value of this option,
     * or do nothing if the given value is invalid
     *
     * @param value The new value of the option
     */
    public void set(T value) {
        if (this.detached) return;

        if (!this.verifyConstraint(value)) return;

        this.backingField.setValue(value);
        this.mirror.set(value);
    }

    /**
     * @return The current value of this option
     */
    public T value() {
        return this.mirror.get();
    }

    /**
     * @return The class of this option's value
     */
    public Class<T> clazz() {
        return this.clazz;
    }

    /**
     * Synchronize the value stored in the backing field
     * and this option's mirror - used for either correcting an
     * invalid value after updating the field or updating the mirror
     */
    public void synchronizeWithBackingField() {
        if (this.detached) return;

        final var fieldValue = (T) this.backingField.getValue();
        if (verifyConstraint(fieldValue)) {
            this.mirror.set(fieldValue);
        } else {
            this.backingField.setValue(this.mirror.get());
        }
    }

    /**
     * Check whether the given value passes the constraint
     * of this option and emit a warning if it does not
     *
     * @param value The value to test
     * @return {@code true} if either the given value
     * passes the constraint put on this option or this
     * option is unconstrained
     */
    public boolean verifyConstraint(T value) {
        if (this.constraint == null) return true;

        final var matched = this.constraint.test(value);
        if (!matched) {
            Owo.LOGGER.warn(
                    "Option {} in config '{}' could not be updated, as the given value '{}' does not match its constraint: {}",
                    this.key, this.configName, value, this.constraint.formatted()
            );
        }

        return matched;
    }

    /**
     * Add an observer function to be run every time
     * the value of this option changes
     */
    public void observe(Consumer<T> observer) {
        this.mirror.observe(observer);
    }

    /**
     * Write the current value of this option into the given buffer
     *
     * @param buf The packet buffer to write to
     */
    void write(FriendlyByteBuf buf) {
        buf.write(this.endec, this.value());
    }

    /**
     * Read a new value of this option from the given buffer
     * and enter a detached state
     *
     * @param buf The packet buffer to read from
     * @return {@code null} if this option was successfully detached,
     * the server's value otherwise
     */
    T read(FriendlyByteBuf buf) {
        final var newValue = buf.read(this.endec);

        if (!Objects.equals(newValue, this.value()) && this.backingField.hasAnnotation(RestartRequired.class)) {
            return newValue;
        }

        this.mirror.set(newValue);
        this.detached = true;

        return null;
    }

    /**
     * @return The serializer for this option's value
     */
    Endec<T> endec() {
        return this.endec;
    }

    /**
     * Reset this option's attached state and synchronize
     * it with the backing field again
     */
    void reattach() {
        if (!this.detached) return;

        this.detached = false;
        this.synchronizeWithBackingField();
    }

    // -------------

    /**
     * @return The translation key of this option
     */
    public String translationKey() {
        return this.translationKey;
    }

    /**
     * @return The name of the config this option is contained in
     */
    public String configName() {
        return configName;
    }

    /**
     * @return The key of this option
     */
    public Key key() {
        return key;
    }

    /**
     * @return The default value of this option
     */
    public T defaultValue() {
        return defaultValue;
    }

    /**
     * @return The field which is backing this option,
     * used for serialization as well as storing the client's
     * value while the option is detached
     */
    public BoundField<T> backingField() {
        return backingField;
    }

    /**
     * @return The constraint placed on the value of this option,
     * or {@code null} if the option is unconstrained
     */
    public ConfigWrapper.@Nullable Constraint constraint() {
        return constraint;
    }

    /**
     * @return {@code true} if this option is currently detached
     */
    public boolean detached() {
        return this.detached;
    }

    /**
     * @return The way in which this option
     * should be synchronized between sever and client
     */
    public SyncMode syncMode() {
        return this.syncMode;
    }

    @Override
    public String toString() {
        return "Option[" +
                "configName=" + configName + ", " +
                "key=" + key + ", " +
                "defaultValue=" + defaultValue + ", " +
                "constraint=" + (constraint == null ? null : constraint.formatted())
                + "]";
    }

    // -------------

    public enum SyncMode {
        /**
         * Do not ever send this option over the network
         */
        NONE,
        /**
         * Only send the client's value to the server,
         * but not vice-versa
         */
        INFORM_SERVER,
        /**
         * Send the client's value to the server
         * <i>and</i> send the server's value back,
         * overriding the client's value
         */
        OVERRIDE_CLIENT;

        public boolean isNone() {
            return this == NONE;
        }
    }

    /**
     * Describes an option's location inside a
     * config, generated from its name a potential
     * parents it is nested in
     *
     * @param path The segments of the path making up this key
     */
    public record Key(String[] path) {

        public static final Key ROOT = new Key(new String[0]);

        public Key(List<String> path) {
            this(path.toArray(String[]::new));
        }

        public Key(String key) {
            this(key.split("\\."));
        }

        /**
         * @return The immediate parent of this key,
         * or {@link #ROOT} if the parent is the root key
         */
        public Key parent() {
            if (this.path.length <= 1) return ROOT;

            var newPath = new String[this.path.length - 1];
            System.arraycopy(this.path, 0, newPath, 0, this.path.length - 1);
            return new Key(newPath);
        }

        /**
         * Create the key for a child of this key
         *
         * @param childName The name of the child
         */
        public Key child(String childName) {
            var newPath = new String[this.path.length + 1];
            System.arraycopy(this.path, 0, newPath, 0, this.path.length);
            newPath[this.path.length] = childName;
            return new Key(newPath);
        }

        /**
         * @return The segments of this key joined with {@code .}
         */
        public String asString() {
            return String.join(".", this.path);
        }

        /**
         * @return The name of the element this key describes,
         * without any of its parents
         */
        public String name() {
            if (this.path.length < 1) return "";
            return this.path[this.path.length - 1];
        }

        /**
         * @return {@code true} if and only if this
         * key is reference-equal to {@link #ROOT}
         */
        public boolean isRoot() {
            return this == ROOT;
        }

        // Records don't play nicely with arrays, thus need to manually
        // declare all the record autogenerated stuff here

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Arrays.equals(path, key.path);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(path);
        }

        @Override
        public String toString() {
            return "Key{" + "path=" + Arrays.toString(path) + '}';
        }
    }

    /**
     * A simple container which stores both a non-static field
     * and an instance of the containing class on which to query
     * values
     *
     * @param owner The owner object which holds the value
     *              the field points to
     * @param field The field itself
     * @param <T>   The type of object this field stores
     */
    @SuppressWarnings("unchecked")
    public record BoundField<T>(Object owner, Field field) {

        public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
            return field.isAnnotationPresent(annotationClass);
        }

        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            return this.field.getAnnotation(annotationClass);
        }

        public T getValue() {
            try {
                return (T) this.field.get(this.owner);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access config option field " + field.getName(), e);
            }
        }

        public void setValue(T value) {
            try {
                this.field.set(this.owner, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not set config option field " + field.getName(), e);
            }
        }
    }
}
