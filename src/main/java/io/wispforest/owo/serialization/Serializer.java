package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.Optional;
import java.util.Set;

public interface Serializer<T> {

    Set<SerializationAttribute> attributes();

    <V> void writeOptional(Endec<V> endec, final Optional<V> optional);

    void writeBoolean(final boolean value);

    void writeByte(final byte value);

    void writeShort(final short value);

    void writeInt(final int value);

    void writeLong(final long value);

    void writeFloat(final float value);

    void writeDouble(final double value);

    void writeString(final String value);

    void writeBytes(final byte[] bytes);

    void writeVarInt(final int value);

    void writeVarLong(final long value);

    <E> Sequence<E> sequence(Endec<E> elementEndec, int size);

    <V> Map<V> map(Endec<V> valueEndec, int size);

    Struct struct();

    T result();

    interface Sequence<E> extends Endable {
        void element(E element);
    }

    interface Map<V> extends Endable {
        void entry(String key, V value);
    }

    interface Struct extends Endable {
        <F> Struct field(String name, Endec<F> endec, F value);
    }
}
