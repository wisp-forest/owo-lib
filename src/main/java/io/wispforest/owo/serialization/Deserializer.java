package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.SerializationAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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

    <V> V tryRead(Function<Deserializer<T>, V> func);

    <E> Sequence<E> sequence(Endec<E> elementEndec);

    <V> Map<V> map(Endec<V> valueEndec);

    Struct struct();

    interface Sequence<E> extends Iterator<E> {

        int size();

        @Override
        boolean hasNext();

        @Override
        E next();
    }

    interface Map<E> extends Iterator<java.util.Map.Entry<String, E>> {

        int size();

        @Override
        boolean hasNext();

        @Override
        java.util.Map.Entry<String, E> next();
    }

    interface Struct {
        /**
         * Method that used to get the value of a field based on order of serialization
         *
         * @param endec The endec for the given field
         * @return the field value
         * @param <F>
         */
        default <F> F field(String name, Endec<F> endec){
            var value = field(name, endec, null);

            if(value == null) throw new StructDeserializerException("Unable to deserialize a required field! [Name: " + name + "]");

            return value;
        }

        /**
         * Method for checking if a field exists based on if the key exists
         * or other method employed by a given Formats {@link Deserializer}
         *
         * @param name Name of the given Field
         * @param endec The endec for the given field
         * @return an optional of the given value if present
         * @param <F>
         */
        <F> F field(String name, Endec<F> endec, @Nullable F defaultValue);
        class StructDeserializerException extends RuntimeException {
            public StructDeserializerException(String message){
                super(message);
            }
        }

    }
}
