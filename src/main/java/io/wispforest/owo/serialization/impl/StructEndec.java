package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.*;

/**
 * Helper Interface for Structs that do not conform to the {@link StructEndecBuilder} format
 */
public interface StructEndec<T> extends Endec<T> {

    @Override
    default void encode(Serializer<?> serializer, T value) {
        try(Serializer.Struct struct = serializer.struct()){
            this.encodeStruct(struct, value);
        }
    }

    @Override
    default T decode(Deserializer<?> deserializer) {
        return this.decodeStruct(deserializer.struct());
    }

    void encodeStruct(Serializer.Struct struct, T value);

    T decodeStruct(Deserializer.Struct struct);

}
