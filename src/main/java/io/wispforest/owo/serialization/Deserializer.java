package io.wispforest.owo.serialization;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface Deserializer<T> {

    Set<SerializationAttribute> attributes();

    byte readByte();
    short readShort();
    int readInt();
    long readLong();
    float readFloat();
    double readDouble();

    int readVarInt();
    long readVarLong();

    boolean readBoolean();
    String readString();
    byte[] readBytes();
    <V> Optional<V> readOptional(Endec<V> endec);

    <E> Sequence<E> sequence(Endec<E> elementEndec);
    <V> Map<V> map(Endec<V> valueEndec);
    Struct struct();

    <V> V tryRead(Function<Deserializer<T>, V> reader);

    interface Sequence<E> extends Iterator<E> {

        int estimatedSize();

        @Override
        boolean hasNext();

        @Override
        E next();
    }

    interface Map<E> extends Iterator<java.util.Map.Entry<String, E>> {

        int estimatedSize();

        @Override
        boolean hasNext();

        @Override
        java.util.Map.Entry<String, E> next();
    }

    interface Struct {
        /**
         * Decode the value of field {@code name} using {@code endec}. If no
         * such field exists in the serialized data, an exception is thrown
         */
        <F> @Nullable F field(String name, Endec<F> endec);

        /**
         * Decode the value of field {@code name} using {@code endec}. If no
         * such field exists in the serialized data, {@code defaultValue} is returned
         */
        <F> @Nullable F field(String name, Endec<F> endec, @Nullable F defaultValue);
    }
}
