package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class KeyedField<F> {

    public MutableObject<F> defaultValue;

    public final String name;
    public final Endec<F> endec;

    protected KeyedField(String name, Endec<F> endec, MutableObject<F> defaultValue) {
        this.name = name;
        this.endec = endec;

        this.defaultValue = defaultValue;
    }

    public <T extends KeyedField<F>> T defaulted(@Nullable F defaultValue){
        return (T) new KeyedField<>(name, endec, new MutableObject<>(defaultValue));
    }

    //--

    public <S> StructField<S, F> struct(Function<S, F> getter){
        return defaultValue == null
                ? StructField.of(name, endec, getter)
                : StructField.defaulted(name, endec, getter, defaultValue.getValue());
    }

    //--

    public static <F> KeyedField<F> of(String name, Endec<F> endec){
        return new KeyedField<>(name, endec, null);
    }

    public static <F> KeyedField<F> defaulted(String name, Endec<F> endec, F defaultValue){
        return new KeyedField<>(name, endec, new MutableObject<>(defaultValue));
    }

    public static <F> KeyedField<Optional<F>> optional(String name, Endec<F> endec){
        return new KeyedField<>(name, endec.ofOptional(), new MutableObject<>(Optional.empty()));
    }

    //--

    public void serializeField(Serializer.Struct serializer, F value){
        serializer.field(name, endec, value);
    }

    public F deserializeField(Deserializer.Struct deserializer){
        return (defaultValue == null)
                ? deserializer.field(name, endec)
                : deserializer.field(name, endec, defaultValue.getValue());
    }

}
