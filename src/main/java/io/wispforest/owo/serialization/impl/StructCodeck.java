package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.*;

/**
 * Helper Interface for Structs that do not conform to the {@link StructCodeckBuilder} format
 */
public interface StructCodeck<T> extends Codeck<T> {

    @Override
    default <E> void encode(Serializer<E> serializer, T value) {
        try(StructSerializer struct = serializer.struct()){
            encode(struct, value);
        }
    }

    @Override
    default <E> T decode(Deserializer<E> deserializer) {
        return decode(deserializer.struct());
    }

    void encode(StructSerializer serializer, T value);

    T decode(StructDeserializer deserializer);

}
