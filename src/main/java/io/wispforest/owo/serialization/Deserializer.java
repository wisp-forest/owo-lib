package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.format.forwarding.ForwardingDeserializer;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

public interface Deserializer<T> {

    default Deserializer<T> withAttributes(SerializationAttribute.Instance... additionalAttributes) {
        if (additionalAttributes.length == 0) return this;
        return ForwardingDeserializer.of(this, additionalAttributes);
    }

    boolean hasAttribute(SerializationAttribute attribute);
    <A> A getAttributeValue(SerializationAttribute.WithValue<A> attribute);

    default <A> A requireAttributeValue(SerializationAttribute.WithValue<A> attribute) {
        if (!this.hasAttribute(attribute)) {
            throw new IllegalStateException("Deserializer did not provide a value for attribute '" + attribute.name + "', which is required for decoding");
        }

        return this.getAttributeValue(attribute);
    }

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
