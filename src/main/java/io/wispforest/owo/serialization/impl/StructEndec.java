package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.*;

/**
 * Helper Interface for Structs that do not conform to the {@link StructEndecBuilder} format
 */
public interface StructEndec<T> extends Endec<T> {

    @Override
    default <E> void encode(Serializer<E> serializer, T value) {
        try(Serializer.Struct struct = serializer.struct()){
            encode(struct, value);
        }
    }

    @Override
    default <E> T decode(Deserializer<E> deserializer) {
        return decode(deserializer.struct());
    }

    void encode(Serializer.Struct serializer, T value);

    T decode(Deserializer.Struct deserializer);

}
