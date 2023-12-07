package io.wispforest.owo.serialization.format.data;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationAttribute;
import io.wispforest.owo.serialization.Serializer;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class DataOutputSerializer<D extends DataOutput> implements Serializer<D> {

    protected final D output;

    public DataOutputSerializer(D output) {
        this.output = output;
    }

    @Override
    public Set<SerializationAttribute> attributes() {
        return null;
    }

    protected void write(Writer writer) {
        try {
            writer.write();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        this.writeBoolean(optional.isPresent());
        optional.ifPresent(value -> endec.encode(this, value));
    }

    @Override
    public void writeBoolean(boolean value) {
        this.write(() -> this.output.writeBoolean(value));
    }

    @Override
    public void writeByte(byte value) {
        this.write(() -> this.output.writeByte(value));
    }

    @Override
    public void writeShort(short value) {
        this.write(() -> this.output.writeShort(value));
    }

    @Override
    public void writeInt(int value) {
        this.write(() -> this.output.writeInt(value));
    }

    @Override
    public void writeLong(long value) {
        this.write(() -> this.output.writeLong(value));
    }

    @Override
    public void writeFloat(float value) {
        this.write(() -> this.output.writeFloat(value));
    }

    @Override
    public void writeDouble(double value) {
        this.write(() -> this.output.writeDouble(value));
    }

    @Override
    public void writeString(String value) {
        this.write(() -> this.output.writeUTF(value));
    }

    @Override
    public void writeBytes(byte[] bytes) {
        this.write(() -> {
            this.writeVarInt(bytes.length);
            this.output.write(bytes);
        });
    }

    @Override
    public void writeVarInt(int value) {
        try {
            while ((value & -128) != 0) {
                this.output.writeByte(value & 127 | 128);
                value >>>= 7;
            }

            this.output.writeByte(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeVarLong(long value) {
        try {
            while ((value & -128L) != 0L) {
                this.output.writeByte((int) (value & 127L) | 128);
                value >>>= 7;
            }

            this.output.writeByte((int) value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public D result() {
        return this.output;
    }

    @Override
    public <V> Map<V> map(Endec<V> valueEndec, int size) {
        this.writeVarInt(size);
        return new Sequence<>(valueEndec);
    }

    @Override
    public <E> Serializer.Sequence<E> sequence(Endec<E> elementEndec, int size) {
        this.writeVarInt(size);
        return new Sequence<>(elementEndec);
    }

    @Override
    public Struct struct() {
        return new Sequence<>(null);
    }

    protected class Sequence<V> implements Serializer.Sequence<V>, Serializer.Struct, Serializer.Map<V> {

        protected final Endec<V> valueEndec;

        protected Sequence(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;
        }

        @Override
        public void element(V element) {
            this.valueEndec.encode(DataOutputSerializer.this, element);
        }

        @Override
        public void entry(String key, V value) {
            DataOutputSerializer.this.writeString(key);
            this.valueEndec.encode(DataOutputSerializer.this, value);
        }

        @Override
        public <F> Struct field(String name, Endec<F> endec, F value) {
            endec.encode(DataOutputSerializer.this, value);
            return this;
        }

        @Override
        public void end() {}
    }

    @FunctionalInterface
    protected interface Writer {
        void write() throws IOException;
    }
}
