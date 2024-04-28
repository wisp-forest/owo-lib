package io.wispforest.owo.serialization.format.data;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationContext;
import io.wispforest.owo.serialization.Serializer;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;

public class DataOutputSerializer<D extends DataOutput> implements Serializer<D> {

    protected final D output;

    protected DataOutputSerializer(D output) {
        this.output = output;
    }

    public static <D extends DataOutput> DataOutputSerializer<D> of(D output) {
        return new DataOutputSerializer<>(output);
    }

    protected void write(Writer writer) {
        try {
            writer.write();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ---

    @Override
    public void writeByte(SerializationContext ctx, byte value) {
        this.write(() -> this.output.writeByte(value));
    }

    @Override
    public void writeShort(SerializationContext ctx, short value) {
        this.write(() -> this.output.writeShort(value));
    }

    @Override
    public void writeInt(SerializationContext ctx, int value) {
        this.write(() -> this.output.writeInt(value));
    }

    @Override
    public void writeLong(SerializationContext ctx, long value) {
        this.write(() -> this.output.writeLong(value));
    }

    @Override
    public void writeFloat(SerializationContext ctx, float value) {
        this.write(() -> this.output.writeFloat(value));
    }

    @Override
    public void writeDouble(SerializationContext ctx, double value) {
        this.write(() -> this.output.writeDouble(value));
    }

    // ---

    @Override
    public void writeVarInt(SerializationContext ctx, int value) {
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
    public void writeVarLong(SerializationContext ctx, long value) {
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

    // ---

    @Override
    public void writeBoolean(SerializationContext ctx, boolean value) {
        this.write(() -> this.output.writeBoolean(value));
    }

    @Override
    public void writeString(SerializationContext ctx, String value) {
        this.write(() -> this.output.writeUTF(value));
    }

    @Override
    public void writeBytes(SerializationContext ctx, byte[] bytes) {
        this.write(() -> {
            this.writeVarInt(ctx, bytes.length);
            this.output.write(bytes);
        });
    }

    @Override
    public <V> void writeOptional(SerializationContext ctx, Endec<V> endec, Optional<V> optional) {
        this.writeBoolean(ctx, optional.isPresent());
        optional.ifPresent(value -> endec.encode(ctx, this, value));
    }

    // ---

    @Override
    public <V> Map<V> map(SerializationContext ctx, Endec<V> valueEndec, int size) {
        this.writeVarInt(ctx, size);
        return new Sequence<>(ctx, valueEndec);
    }

    @Override
    public <E> Serializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec, int size) {
        this.writeVarInt(ctx, size);
        return new Sequence<>(ctx, elementEndec);
    }

    @Override
    public Struct struct() {
        return new Sequence<>(null, null);
    }

    // ---

    @Override
    public D result() {
        return this.output;
    }

    // ---

    protected class Sequence<V> implements Serializer.Sequence<V>, Serializer.Struct, Serializer.Map<V> {

        private final SerializationContext ctx;
        protected final Endec<V> valueEndec;

        protected Sequence(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;
        }

        @Override
        public void element(V element) {
            this.valueEndec.encode(this.ctx, DataOutputSerializer.this, element);
        }

        @Override
        public void entry(String key, V value) {
            DataOutputSerializer.this.writeString(this.ctx, key);
            this.valueEndec.encode(this.ctx, DataOutputSerializer.this, value);
        }

        @Override
        public <F> Struct field(String name, SerializationContext ctx, Endec<F> endec, F value) {
            endec.encode(ctx, DataOutputSerializer.this, value);
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
