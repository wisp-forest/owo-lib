package io.wispforest.owo.serialization.endec;

import io.wispforest.owo.serialization.Endec;

import java.util.function.Supplier;

public final class KeyedEndec<F> {

    private final String key;
    private final Endec<F> endec;
    private final Supplier<F> defaultValueFactory;

    public KeyedEndec(String key, Endec<F> endec, Supplier<F> defaultValueFactory) {
        this.key = key;
        this.endec = endec;
        this.defaultValueFactory = defaultValueFactory;
    }

    public KeyedEndec(String key, Endec<F> endec, F defaultValue) {
        this(key, endec, () -> defaultValue);
    }

    public String key() {
        return this.key;
    }

    public Endec<F> endec() {
        return this.endec;
    }

    public F defaultValue() {
        return this.defaultValueFactory.get();
    }
}
