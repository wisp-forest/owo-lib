package io.wispforest.owo.serialization.impl.data;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public abstract class DataOutputSerializer<D extends DataOutput> implements Serializer<D> {

    public abstract D get();

    @Override
    public Set<SerializationAttribute> attributes() {
        return null;
    }

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        writeBoolean(optional.isPresent());

        optional.ifPresent(v -> endec.encode(this, v));
    }

    @Override
    public void writeBoolean(boolean value) {
        try {
            get().writeBoolean(value);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeByte(byte value) {
        try {
            get().writeByte(value);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeShort(short value) {
        try {
            get().writeShort(value);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeInt(int value) {
        try {
            get().writeInt(value);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeLong(long value) {
        try {
            get().writeLong(value);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeFloat(float value) {
        try {
            get().writeFloat(value);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeDouble(double value) {
        try {
            get().writeDouble(value);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeString(String value) {
        try {
            get().writeUTF(value);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeBytes(byte[] bytes) {
        writeVarInt(bytes.length);
        try {
            get().write(bytes);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeVarInt(int value) {
        writeInt(value);
    }

    @Override
    public void writeVarLong(long value) {
        writeLong(value);
    }

    @Override
    public D result() {
        return get();
    }

    @Override
    public <V> MapSerializer<V> map(Endec<V> valueEndec, int size) {
        return (MapSerializer<V>) sequence(valueEndec, size);
    }

    @Override
    public <E> SequenceSerializer<E> sequence(Endec<E> elementEndec, int size) {
        writeVarInt(size);

        return new DataOutputSequenceSerializer<>(elementEndec);
    }

    @Override
    public StructSerializer struct() {
        return new DataOutputSequenceSerializer(null);
    }

    public class DataOutputSequenceSerializer<V> implements SequenceSerializer<V>, StructSerializer, MapSerializer<V> {

        private final Endec<V> valueEndec;

        public DataOutputSequenceSerializer(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;
        }

        @Override
        public void element(V element) {
            field("", valueEndec, element);
        }

        @Override
        public void entry(String key, V value) {
            DataOutputSerializer.this.writeString(key);
            field(key, valueEndec, value);
        }

        @Override
        public <F> StructSerializer field(String name, Endec<F> endec, F value) {
            endec.encode(DataOutputSerializer.this, value);

            return this;
        }

        @Override public void end() {}
    }

}
