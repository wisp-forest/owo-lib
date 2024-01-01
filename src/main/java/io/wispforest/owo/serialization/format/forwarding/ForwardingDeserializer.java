package io.wispforest.owo.serialization.format.forwarding;

import com.google.common.collect.ImmutableSet;
import io.wispforest.owo.serialization.*;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ForwardingDeserializer<T> implements Deserializer<T> {

    private final Set<SerializationAttribute> attributes;
    private final Deserializer<T> delegate;

    protected ForwardingDeserializer(Deserializer<T> delegate, Set<SerializationAttribute> attributes) {
        this.delegate = delegate;
        this.attributes = attributes;
    }

    public static <T> ForwardingDeserializer<T> of(Deserializer<T> delegate, SerializationAttribute... assumedAttributes) {
        var attributes = ImmutableSet.<SerializationAttribute>builder()
                .addAll(delegate.attributes())
                .add(assumedAttributes)
                .build();

        return (delegate instanceof SelfDescribedDeserializer<T> selfDescribedDeserializer)
                ? new ForwardingSelfDescribedDeserializer<>(selfDescribedDeserializer, attributes)
                : new ForwardingDeserializer<>(delegate, attributes);
    }

    public Deserializer<T> delegate() {
        return this.delegate;
    }

    //--

    @Override
    public Set<SerializationAttribute> attributes() {
        return attributes;
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
        private ForwardingSelfDescribedDeserializer(Deserializer<T> delegate, Set<SerializationAttribute> attributes) {
            super(delegate, attributes);
        }

        @Override
        public <S> void readAny(Serializer<S> visitor) {
            ((SelfDescribedDeserializer<T>) this.delegate()).readAny(visitor);
        }
    }
}
