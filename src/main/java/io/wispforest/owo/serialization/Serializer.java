package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.format.forwarding.ForwardingSerializer;
import io.wispforest.owo.serialization.util.Endable;

import java.util.Optional;

public interface Serializer<T> {

    default Serializer<T> withAttributes(SerializationAttribute.Instance... additionalAttributes) {
        if (additionalAttributes.length == 0) return this;
        return ForwardingSerializer.of(this, additionalAttributes);
    }

    boolean hasAttribute(SerializationAttribute attribute);
    <A> A getAttributeValue(SerializationAttribute.WithValue<A> attribute);

    default <A> A requireAttributeValue(SerializationAttribute.WithValue<A> attribute) {
        if (!this.hasAttribute(attribute)) {
            throw new IllegalStateException("Serializer did not provide a value for attribute '" + attribute.name + "', which is required for encoding");
        }

        return this.getAttributeValue(attribute);
    }

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
