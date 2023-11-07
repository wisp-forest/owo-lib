package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.KeyedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface MapCarrier {

    // Interface specification

    default <T> T get(@NotNull KeyedField<T> key){
        throw new IllegalStateException("Interface default method called");
    }

    default <T> void put(@NotNull KeyedField<T> key, @NotNull T value){
        throw new IllegalStateException("Interface default method called");
    }

    default <T> void delete(@NotNull KeyedField<T> key){
        throw new IllegalStateException("Interface default method called");
    }

    default <T> boolean has(@NotNull KeyedField<T> key){
        throw new IllegalStateException("Interface default method called");
    }

    // Default implementations

    default <T> T getOr(@NotNull KeyedField<T> key, @Nullable T defaultValue) {
        return this.has(key) ? this.get(key) : defaultValue;
    }

    default <T> void putIfNotNull(@NotNull KeyedField<T> key, @Nullable T value) {
        if (value == null) return;
        this.put(key, value);
    }

    default <T> void copy(@NotNull KeyedField<T> key, @NotNull MapCarrier other) {
        other.put(key, this.get(key));
    }

    default <T> void copyIfPresent(@NotNull KeyedField<T> key, @NotNull MapCarrier other) {
        if (!this.has(key)) return;
        this.copy(key, other);
    }

    default <T> void mutate(@NotNull KeyedField<T> key, @NotNull Function<T, T> mutator) {
        this.put(key, mutator.apply(this.get(key)));
    }
}
