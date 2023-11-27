package io.wispforest.owo.serialization.impl.forwarding;

import com.google.common.collect.ImmutableSet;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.Optional;
import java.util.Set;

public class ForwardingSerializer<T> implements Serializer<T> {

    private final Set<SerializationAttribute> attributes;
    private final Serializer<T> innerSerializer;

    private ForwardingSerializer(Serializer<T> wrappedSerializer, boolean humanReadable){
        this.innerSerializer = wrappedSerializer;

        var set = ImmutableSet.<SerializationAttribute>builder();

        if(this.innerSerializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING)){
            set.add(SerializationAttribute.SELF_DESCRIBING);
        }

        if(humanReadable) set.add(SerializationAttribute.HUMAN_READABLE);

        this.attributes = set.build();
    }

    public Serializer<T> delegate(){
        return this.innerSerializer;
    }

    public static <T> ForwardingSerializer<T> of(Serializer<T> serializer){
        return new ForwardingSerializer<>(serializer, false);
    }

    public static <T> ForwardingSerializer<T> humanReadable(Serializer<T> serializer){
        return new ForwardingSerializer<>(serializer, true);
    }

    //--

    @Override
    public Set<SerializationAttribute> attributes() {
        return attributes;
    }

    @Override
    public void writeByte(byte value) {
        this.innerSerializer.writeByte(value);
    }

    @Override
    public void writeShort(short value) {
        this.innerSerializer.writeShort(value);
    }

    @Override
    public void writeInt(int value) {
        this.innerSerializer.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
        this.innerSerializer.writeLong(value);
    }

    @Override
    public void writeFloat(float value) {
        this.innerSerializer.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
        this.innerSerializer.writeDouble(value);
    }

    @Override
    public void writeVarInt(int value) {
        this.innerSerializer.writeVarInt(value);
    }

    @Override
    public void writeVarLong(long value) {
        this.innerSerializer.writeVarLong(value);
    }

    @Override
    public void writeBoolean(boolean value) {
        this.innerSerializer.writeBoolean(value);
    }

    @Override
    public void writeString(String value) {
        this.innerSerializer.writeString(value);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        this.innerSerializer.writeBytes(bytes);
    }

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        this.innerSerializer.writeOptional(endec, optional);
    }

    @Override
    public <E> Sequence<E> sequence(Endec<E> elementEndec, int size) {
        return this.innerSerializer.sequence(elementEndec, size);
    }

    @Override
    public <V> Map<V> map(Endec<V> valueEndec, int size) {
        return this.innerSerializer.map(valueEndec, size);
    }

    @Override
    public Struct struct() {
        return this.innerSerializer.struct();
    }

    @Override
    public T result() {
        return this.innerSerializer.result();
    }
}
