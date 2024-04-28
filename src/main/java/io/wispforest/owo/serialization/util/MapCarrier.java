package io.wispforest.owo.serialization.util;

import io.wispforest.owo.serialization.SerializationContext;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface MapCarrier {

    /**
     * Get the value stored under {@code key} in this object's associated map.
     * If no such value exists, the default value of {@code key} is returned
     * <p>
     * Any exceptions thrown during decoding are propagated to the caller
     */
    default <T> T getWithErrors(@NotNull KeyedEndec<T> key, SerializationContext ctx) {
        throw new UnsupportedOperationException("Interface default method called");
    }

    default <T> T getWithErrors(@NotNull KeyedEndec<T> key) {
        return this.getWithErrors(key, SerializationContext.empty());
    }

    /**
     * Store {@code value} under {@code key} in this object's associated map
     */
    default <T> void put(@NotNull KeyedEndec<T> key, @NotNull T value, SerializationContext ctx) {
        throw new UnsupportedOperationException("Interface default method called");
    }

    default <T> void put(@NotNull KeyedEndec<T> key, @NotNull T value) {
        this.put(key, value, SerializationContext.empty());
    }

    /**
     * Delete the value stored under {@code key} from this object's associated map,
     * if it is present
     */
    default <T> void delete(@NotNull KeyedEndec<T> key) {
        throw new UnsupportedOperationException("Interface default method called");
    }

    /**
     * Test whether there is a value stored under {@code key} in this object's associated map
     */
    default <T> boolean has(@NotNull KeyedEndec<T> key) {
        throw new UnsupportedOperationException("Interface default method called");
    }

    // ---

    /**
     * Get the value stored under {@code key} in this object's associated map.
     * If no such value exists <i>or</i> an exception is thrown during decoding,
     * the default value of {@code key} is returned
     */
    default <T> T get(@NotNull KeyedEndec<T> key) {
        try {
            return this.getWithErrors(key);
        } catch (Exception e) {
            return key.defaultValue();
        }
    }

    /**
     * Get the value stored under {@code key} in this object's associated map.
     * If no such value exists <i>or</i> an exception is thrown during decoding,
     * the default value of {@code key} is returned
     */
    default <T> T get(@NotNull KeyedEndec<T> key, SerializationContext ctx) {
        try {
            return this.getWithErrors(key, ctx);
        } catch (Exception e) {
            return key.defaultValue();
        }
    }

    /**
     * If {@code value} is not {@code null}, store it under {@code key} in this
     * object's associated map
     */
    default <T> void putIfNotNull(@NotNull KeyedEndec<T> key, @Nullable T value) {
        if (value == null) return;
        this.put(key, value);
    }

    /**
     * Store the value associated with {@code key} in this object's associated map
     * into the associated map of {@code other} under {@code key}
     * <p>
     * Importantly, this does not copy the value itself - be careful with mutable types
     */
    default <T> void copy(@NotNull KeyedEndec<T> key, @NotNull MapCarrier other) {
        other.put(key, this.get(key));
    }

    /**
     * Like {@link #copy(KeyedEndec, MapCarrier)}, but only if this object's associated map
     * has a value stored under {@code key}
     */
    default <T> void copyIfPresent(@NotNull KeyedEndec<T> key, @NotNull MapCarrier other) {
        if (!this.has(key)) return;
        this.copy(key, other);
    }

    /**
     * Get the value stored under {@code key} in this object's associated map, apply
     * {@code mutator} to it and store the result under {@code key}
     */
    default <T> void mutate(@NotNull KeyedEndec<T> key, @NotNull Function<T, T> mutator) {
        this.put(key, mutator.apply(this.get(key)));
    }
}
