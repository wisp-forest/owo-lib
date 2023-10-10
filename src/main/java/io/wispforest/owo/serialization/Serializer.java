package io.wispforest.owo.serialization;

import java.util.Optional;

public interface Serializer<T> {

    <V> void writeOptional(Codeck<V> codeck, final Optional<V> optional);

    void writeBoolean(final boolean value);

    void writeByte(final byte value);

    void writeShort(final short value);

    void writeInt(final int value);

    void writeLong(final long value);

    void writeFloat(final float value);

    void writeDouble(final double value);

    void writeString(final String value);

    void writeBytes(final byte[] bytes);

    <E> SequenceSerializer<E> sequence(Codeck<E> elementCodec, int length);

    <V> MapSerializer<V> map(Codeck<V> valueCodec, int length);

    StructSerializer struct();

    T result();
}
