package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.Optional;
import java.util.Set;

public interface Deserializer<T> {

    Set<SerializationAttribute> attributes();

    <V> Optional<V> readOptional(Endec<V> endec);

    boolean readBoolean();

    byte readByte();

    short readShort();

    int readInt();

    long readLong();

    float readFloat();

    double readDouble();

    String readString();

    byte[] readBytes();

    int readVarInt();

    long readVarLong();

    <E> SequenceDeserializer<E> sequence(Endec<E> elementEndec);

    <V> MapDeserializer<V> map(Endec<V> valueEndec);

    StructDeserializer struct();
}
