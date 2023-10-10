package io.wispforest.owo.serialization;

import java.util.Optional;

public interface Deserializer<T> {

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

    <E> SequenceDeserializer<E> sequence(Codeck<E> elementCodec);

    <V> MapDeserializer<V> map(Codeck<V> valueCodec);

    StructDeserializer struct();
}
