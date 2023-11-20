package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class StructField<S, F> extends KeyedField<F> {

    public final Function<S, F> getter;

    protected StructField(String name, Endec<F> endec, Function<S, F> getter, @Nullable MutableObject<F> defaultValue) {
        super(name, endec, defaultValue);

        this.getter = getter;
    }

    @Override
    public <T extends KeyedField<F>> T defaulted(@Nullable F defaultValue) {
        return (T) new StructField<>(name, endec, getter, new MutableObject<>(defaultValue));
    }

    public static <S, F> StructField<S, F> of(String name, Endec<F> endec, Function<S, F> getter){
        return new StructField<>(name, endec, getter, null);
    }

    public static <S, F> StructField<S, F> defaulted(String name, Endec<F> endec, Function<S, F> getter, F defaultValue){
        return new StructField<>(name, endec, getter, new MutableObject<>(defaultValue));
    }

    public static <S, F> StructField<S, Optional<F>> optional(String name, Endec<F> endec, Function<S, Optional<F>> getter){
        return new StructField<>(name, endec.optionalOf(), getter, new MutableObject<>(Optional.empty()));
    }

    //--

    public void serializeFieldInst(Serializer.Struct serializer, S instance){
        super.serializeField(serializer, getter.apply(instance));
    }
}
