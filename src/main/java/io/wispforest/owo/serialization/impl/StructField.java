package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Codeck;
import io.wispforest.owo.serialization.StructDeserializer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class StructField<S, F> {

    public MutableObject<F> defaultValue = null;

    public final String name;
    public final Codeck<F> codec;
    public final Function<S, F> getter;

    private StructField(String name, Codeck<F> codec, Function<S, F> getter) {
        this.name = name;
        this.codec = codec;
        this.getter = getter;
    }

    private StructField<S, F> defaultValue(@Nullable F defaultValue){
        this.defaultValue = new MutableObject<>(defaultValue);

        return this;
    }

    public static <S, F> StructField<S, F> of(String name, Codeck<F> codec, Function<S, F> getter){
        return new StructField<>(name, codec, getter);
    }

    public static <S, F> StructField<S, F> defaulted(String name, Codeck<F> codec, Function<S, F> getter, F defaultValue){
        return new StructField<>(name, codec, getter).defaultValue(defaultValue);
    }

    public static <S, F> StructField<S, Optional<F>> optional(String name, Codeck<F> codec, Function<S, Optional<F>> getter){
        return new StructField<>(name, codec.ofOptional(), getter).defaultValue(Optional.empty());
    }

    public F deserialize(StructDeserializer deserializer){
        return (defaultValue == null)
                ? deserializer.field(name, codec)
                : deserializer.field(name, codec, defaultValue.getValue());
    }
}
