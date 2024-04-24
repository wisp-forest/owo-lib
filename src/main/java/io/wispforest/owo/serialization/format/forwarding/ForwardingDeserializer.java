package io.wispforest.owo.serialization.format.forwarding;

import io.wispforest.owo.serialization.*;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

public class ForwardingDeserializer<T> implements Deserializer<T> {

    private final java.util.Map<SerializationAttribute, Object> additionalAttributes;
    private final Deserializer<T> delegate;

    protected ForwardingDeserializer(Deserializer<T> delegate, java.util.Map<SerializationAttribute, Object> additionalAttributes) {
        this.delegate = delegate;
        this.additionalAttributes = additionalAttributes;
    }

    public static <T> ForwardingDeserializer<T> of(Deserializer<T> delegate, SerializationAttribute.Instance... additionalAttributes) {
        var attributes = new HashMap<SerializationAttribute, Object>();
        for (var instance : additionalAttributes) {
            attributes.put(instance.attribute(), instance.value());
        }

        return (delegate instanceof SelfDescribedDeserializer<T> selfDescribedDeserializer)
                ? new ForwardingSelfDescribedDeserializer<>(selfDescribedDeserializer, attributes)
                : new ForwardingDeserializer<>(delegate, attributes);
    }

    public Deserializer<T> delegate() {
        return this.delegate;
    }

    //--

    @Override
    public boolean hasAttribute(SerializationAttribute attribute) {
        return this.additionalAttributes.containsKey(attribute) || this.delegate.hasAttribute(attribute);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A getAttributeValue(SerializationAttribute.WithValue<A> attribute) {
        return this.additionalAttributes.containsKey(attribute)
                ? (A) this.additionalAttributes.get(attribute)
                : this.delegate.getAttributeValue(attribute);
    }

    @Override
    public byte readByte() {
        return this.delegate.readByte();
    }

    @Override
    public short readShort() {
        return this.delegate.readShort();
    }

    @Override
    public int readInt() {
        return this.delegate.readInt();
    }

    @Override
    public long readLong() {
        return this.delegate.readLong();
    }

    @Override
    public float readFloat() {
        return this.delegate.readFloat();
    }

    @Override
    public double readDouble() {
        return this.delegate.readDouble();
    }

    @Override
    public int readVarInt() {
        return this.delegate.readVarInt();
    }

    @Override
    public long readVarLong() {
        return this.delegate.readVarLong();
    }

    @Override
    public boolean readBoolean() {
        return this.delegate.readBoolean();
    }

    @Override
    public String readString() {
        return this.delegate.readString();
    }

    @Override
    public byte[] readBytes() {
        return this.delegate.readBytes();
    }

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        return this.delegate.readOptional(endec);
    }

    @Override
    public <E> Sequence<E> sequence(Endec<E> elementEndec) {
        return this.delegate.sequence(elementEndec);
    }

    @Override
    public <V> Map<V> map(Endec<V> valueEndec) {
        return this.delegate.map(valueEndec);
    }

    @Override
    public Struct struct() {
        return this.delegate.struct();
    }

    @Override
    public <V> V tryRead(Function<Deserializer<T>, V> reader) {
        return this.delegate.tryRead(reader);
    }

    private static class ForwardingSelfDescribedDeserializer<T> extends ForwardingDeserializer<T> implements SelfDescribedDeserializer<T> {
        private ForwardingSelfDescribedDeserializer(Deserializer<T> delegate, java.util.Map<SerializationAttribute, Object> attributes) {
            super(delegate, attributes);
        }

        @Override
        public <S> void readAny(Serializer<S> visitor) {
            ((SelfDescribedDeserializer<T>) this.delegate()).readAny(visitor);
        }
    }
}
