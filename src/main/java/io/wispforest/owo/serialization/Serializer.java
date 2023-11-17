package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.Optional;
import java.util.Set;

public interface Serializer<T> {

    Set<SerializationAttribute> attributes();

    void writeByte(byte value);
    void writeShort(short value);
    void writeInt(int value);
    void writeLong(long value);
    void writeFloat(float value);
    void writeDouble(double value);

    void writeVarInt(int value);
    void writeVarLong(long value);

    void writeBoolean(boolean value);
    void writeString(String value);
    void writeBytes(byte[] bytes);
    <V> void writeOptional(Endec<V> endec, Optional<V> optional);

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
