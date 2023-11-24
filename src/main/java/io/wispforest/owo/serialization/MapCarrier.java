package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.KeyedEndec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface MapCarrier {

    // Interface specification

    default <T> T getWithErrors(@NotNull KeyedEndec<T> key) {
        throw new UnsupportedOperationException("Interface default method called");
    }

    default <T> void put(@NotNull KeyedEndec<T> key, @NotNull T value) {
        throw new UnsupportedOperationException("Interface default method called");
    }

    default <T> void delete(@NotNull KeyedEndec<T> key) {
        throw new UnsupportedOperationException("Interface default method called");
    }

    default <T> boolean has(@NotNull KeyedEndec<T> key) {
        throw new UnsupportedOperationException("Interface default method called");
    }

    // Default implementations

    default <T> T get(@NotNull KeyedEndec<T> key) {
        try {
            return this.getWithErrors(key);
        } catch (Exception e) {
            return key.defaultValue();
        }
    }

    default <T> void putIfNotNull(@NotNull KeyedEndec<T> key, @Nullable T value) {
        if (value == null) return;
        this.put(key, value);
    }

    default <T> void copy(@NotNull KeyedEndec<T> key, @NotNull MapCarrier other) {
        other.put(key, this.get(key));
    }

    default <T> void copyIfPresent(@NotNull KeyedEndec<T> key, @NotNull MapCarrier other) {
        if (!this.has(key)) return;
        this.copy(key, other);
    }

    default <T> void mutate(@NotNull KeyedEndec<T> key, @NotNull Function<T, T> mutator) {
        this.put(key, mutator.apply(this.get(key)));
    }
}
