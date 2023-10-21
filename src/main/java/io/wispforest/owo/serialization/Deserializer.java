package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.Optional;
import java.util.Set;

public interface Deserializer<T> {

    Set<SerializationAttribute> attributes();

    Deserializer<T> addAttribute(SerializationAttribute ...attributes);

    <V> Optional<V> readOptional(Codeck<V> codeck);

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

    <E> SequenceDeserializer<E> sequence(Codeck<E> elementCodec);

    <V> MapDeserializer<V> map(Codeck<V> valueCodec);

    StructDeserializer struct();
}
