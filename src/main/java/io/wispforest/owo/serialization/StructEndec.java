package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.endec.StructEndecBuilder;

/**
 * Helper Interface for Structs that do not conform to the {@link StructEndecBuilder} format
 */
public interface StructEndec<T> extends Endec<T> {

    void encodeStruct(Serializer.Struct struct, T value);

    T decodeStruct(Deserializer.Struct struct);

    @Override
    default void encode(Serializer<?> serializer, T value) {
        try (var struct = serializer.struct()) {
            this.encodeStruct(struct, value);
        }
    }

    @Override
    default T decode(Deserializer<?> deserializer) {
        return this.decodeStruct(deserializer.struct());
    }
}
