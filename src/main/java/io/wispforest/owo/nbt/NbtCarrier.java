package io.wispforest.owo.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface NbtCarrier {

    // Interface specification

    default <T> T get(@NotNull NbtKey<T> key) {
        throw new IllegalStateException("Interface default method called");
    }

    default <T> void put(@NotNull NbtKey<T> key, @NotNull T value) {
        throw new IllegalStateException("Interface default method called");
    }

    default <T> void delete(@NotNull NbtKey<T> key) {
        throw new IllegalStateException("Interface default method called");
    }

    default <T> boolean has(@NotNull NbtKey<T> key) {
        throw new IllegalStateException("Interface default method called");
    }

    // Default implementations

    default <T> T getOr(@NotNull NbtKey<T> key, @Nullable T defaultValue) {
        return this.has(key) ? this.get(key) : defaultValue;
    }

    default <T> void putIfNotNull(@NotNull NbtKey<T> key, @Nullable T value) {
        if (value == null) return;
        this.put(key, value);
    }

    default <T> void copy(@NotNull NbtKey<T> key, @NotNull NbtCarrier other) {
        other.put(key, this.get(key));
    }

    default <T> void copyIfPresent(@NotNull NbtKey<T> key, @NotNull NbtCarrier other) {
        if (!this.has(key)) return;
        this.copy(key, other);
    }

    default <T> void mutate(@NotNull NbtKey<T> key, @NotNull Function<T, T> mutator) {
        this.put(key, mutator.apply(this.get(key)));
    }

}
