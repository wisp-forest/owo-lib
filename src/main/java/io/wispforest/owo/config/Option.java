package io.wispforest.owo.config;

import io.wispforest.owo.Owo;
import io.wispforest.owo.util.Observable;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Describes a single option in a config. Instances
 * of this class keep a reference to the field in
 * the model class which stores the value used for serialization
 *
 * @param configName   The name of the config this option is contained in
 * @param key          The key of this option
 * @param defaultValue The default value of this option
 * @param events       A mirror of the value of this option, used for
 *                     emitting events when it changes as well as correcting
 *                     invalid values after deserialization
 * @param backingField The backing field in the config model class
 *                     which this option describes
 * @param constraint   The constraint placed on the value of this option,
 *                     or {@code null} if the option is unconstrained
 */
public record Option<T>(
        String configName,
        Key key,
        T defaultValue,
        Observable<T> events,
        BoundField<T> backingField,
        @Nullable ConfigWrapper.Constraint constraint
) {

    /**
     * Update the current value of this option,
     * or do nothing if the given value is invalid
     *
     * @param value The new value of the option
     */
    public void set(T value) {
        if (!this.verifyConstraint(value)) return;

        this.backingField.setValue(value);
        this.events.set(value);
    }

    /**
     * @return The current value of this option
     */
    public T value() {
        return this.events.get();
    }

    /**
     * @return The class of this option's value
     */
    @SuppressWarnings("unchecked")
    public Class<T> clazz() {
        return (Class<T>) this.backingField.field().getType();
    }

    /**
     * Synchronize the value stored in the backing field
     * and this option's mirror - used for either correcting an
     * invalid value after updating the field or updating the mirror
     */
    public void synchronizeWithBackingField() {
        final var fieldValue = (T) this.backingField.getValue();
        if (verifyConstraint(fieldValue)) {
            this.events.set(fieldValue);
        } else {
            this.backingField.setValue(this.events.get());
        }
    }

    /**
     * Check whether the given value passes the constraint,
     * of this option and emit a warning if it does not
     *
     * @param value The value to test
     * @return {@code true} if either the given value
     * passes the constraint put on this option or this
     * option is unconstrained
     */
    public boolean verifyConstraint(T value) {
        if (constraint == null) return true;

        final var matched = constraint.test(value);
        if (!matched) {
            Owo.LOGGER.warn(
                    "Option {} in config '{}' could not be updated, as the given value '{}' does not match its constraint: {}",
                    key, configName, value, constraint.formatted()
            );
        }

        return matched;
    }

    /**
     * @return The translation key of this option
     */
    public String translationKey() {
        return "text.config." + this.configName + ".option." + this.key.asString();
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
