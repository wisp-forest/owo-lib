package io.wispforest.owo.serialization.format.forwarding;

import com.google.common.collect.ImmutableSet;
import io.wispforest.owo.serialization.*;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ForwardingDeserializer<T> implements Deserializer<T> {

    private final Set<SerializationAttribute> attributes;
    private final Deserializer<T> wrappedSerializer;

    protected ForwardingDeserializer(Deserializer<T> wrappedSerializer, boolean humanReadable) {
        this.wrappedSerializer = wrappedSerializer;

        var set = ImmutableSet.<SerializationAttribute>builder();

        if (this.wrappedSerializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING)) {
            set.add(SerializationAttribute.SELF_DESCRIBING);
        }

        if (humanReadable) {
            set.add(SerializationAttribute.HUMAN_READABLE);
        }

        this.attributes = set.build();
    }

    public static <T> ForwardingDeserializer<T> of(Deserializer<T> deserializer) {
        return create(deserializer, false);
    }

    public static <T> ForwardingDeserializer<T> humanReadable(Deserializer<T> deserializer) {
        return create(deserializer, true);
    }

    private static <T> ForwardingDeserializer<T> create(Deserializer<T> deserializer, boolean humanReadable) {
        return (deserializer instanceof SelfDescribedDeserializer<T> selfDescribedDeserializer)
                ? new ForwardingSelfDescribedDeserializer<>(selfDescribedDeserializer, humanReadable)
                : new ForwardingDeserializer<>(deserializer, humanReadable);
    }

    public Deserializer<T> delegate() {
        return this.wrappedSerializer;
    }

    //--

    @Override
    public Set<SerializationAttribute> attributes() {
        return attributes;
    }

    @Override
    public byte readByte() {
        return this.wrappedSerializer.readByte();
    }

    @Override
    public short readShort() {
        return this.wrappedSerializer.readShort();
    }

    @Override
    public int readInt() {
        return this.wrappedSerializer.readInt();
    }

    @Override
    public long readLong() {
        return this.wrappedSerializer.readLong();
    }

    @Override
    public float readFloat() {
        return this.wrappedSerializer.readFloat();
    }

    @Override
    public double readDouble() {
        return this.wrappedSerializer.readDouble();
    }

    @Override
    public int readVarInt() {
        return this.wrappedSerializer.readVarInt();
    }

    @Override
    public long readVarLong() {
        return this.wrappedSerializer.readVarLong();
    }

    @Override
    public boolean readBoolean() {
        return this.wrappedSerializer.readBoolean();
    }

    @Override
    public String readString() {
        return this.wrappedSerializer.readString();
    }

    @Override
    public byte[] readBytes() {
        return this.wrappedSerializer.readBytes();
    }

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        return this.wrappedSerializer.readOptional(endec);
    }

    @Override
    public <E> Sequence<E> sequence(Endec<E> elementEndec) {
        return this.wrappedSerializer.sequence(elementEndec);
    }

    @Override
    public <V> Map<V> map(Endec<V> valueEndec) {
        return this.wrappedSerializer.map(valueEndec);
    }

    @Override
    public Struct struct() {
        return this.wrappedSerializer.struct();
    }

    @Override
    public <V> V tryRead(Function<Deserializer<T>, V> reader) {
        return this.wrappedSerializer.tryRead(reader);
    }

    public static class ForwardingSelfDescribedDeserializer<T> extends ForwardingDeserializer<T> implements SelfDescribedDeserializer<T> {
        private ForwardingSelfDescribedDeserializer(SelfDescribedDeserializer<T> wrappedSerializer, boolean humanReadable) {
            super(wrappedSerializer, humanReadable);
        }

        @Override
        public <S> void readAny(Serializer<S> visitor) {
            ((SelfDescribedDeserializer<T>) this.delegate()).readAny(visitor);
        }
    }
}
