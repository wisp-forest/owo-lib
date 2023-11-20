package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class StructField<S, F> {

    private final String name;
    private final Endec<F> endec;
    private final Function<S, F> getter;
    private final @Nullable MutableObject<F> defaultValue;

    private StructField(String name, Endec<F> endec, Function<S, F> getter, @Nullable MutableObject<F> defaultValue) {
        this.name = name;
        this.endec = endec;
        this.getter = getter;
        this.defaultValue = defaultValue;
    }

    public StructField(String name, Endec<F> endec, Function<S, F> getter, @Nullable F defaultValue) {
        this(name, endec, getter, new MutableObject<>(defaultValue));
    }

    public StructField(String name, Endec<F> endec, Function<S, F> getter) {
        this(name, endec, getter, (MutableObject<F>) null);
    }

    public void encodeField(Serializer.Struct struct, S instance) {
        struct.field(this.name, this.endec, this.getter.apply(instance));
    }

    public F decodeField(Deserializer.Struct struct) {
        return this.defaultValue != null
                ? struct.field(this.name, this.endec, this.defaultValue.getValue())
                : struct.field(this.name, this.endec);
    }
}
