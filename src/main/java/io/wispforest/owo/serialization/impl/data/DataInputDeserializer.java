package io.wispforest.owo.serialization.impl.data;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public abstract class DataInputDeserializer<D extends DataInput> implements Deserializer<D> {

    public abstract D get();

    @Override
    public Set<SerializationAttribute> attributes() {
        return Set.of();
    }

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        var bl = readBoolean();

        return Optional.ofNullable(bl ? endec.decode(this) : null);
    }

    @Override
    public boolean readBoolean() {
        try {
            return get().readBoolean();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte readByte() {
        try {
            return get().readByte();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public short readShort() {
        try {
            return get().readShort();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readInt() {
        try {
            return get().readInt();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readLong() {
        try {
            return get().readLong();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public float readFloat() {
        try {
            return get().readFloat();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public double readDouble() {
        try {
            return get().readDouble();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readString() {
        try {
            return get().readUTF();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] readBytes() {
        var array = new byte[readVarInt()];

        try {
        get().readFully(array);
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        return array;
    }

    @Override
    public int readVarInt() {
        return readInt();
    }

    @Override
    public long readVarLong() {
        return readLong();
    }

    @Override
    public <V> V tryRead(Function<Deserializer<D>, V> func) {
        return func.apply(this);
    }

    @Override
    public <E> Deserializer.Sequence<E> sequence(Endec<E> elementEndec) {
        return new Sequence<E>().valueEndec(elementEndec, readVarInt());
    }

    @Override
    public <V> Deserializer.Map<V> map(Endec<V> valueEndec) {
        return new Map<>(valueEndec, readVarInt());
    }

    @Override
    public Struct struct() {
        return new Sequence<>();
    }

    private class Sequence<V> implements Deserializer.Sequence<V>, Struct {

        private int maxSize;
        private Endec<V> valueEndec;

        private int index = 0;

        private Sequence<V> valueEndec(Endec<V> valueEndec, int maxSize) {
            this.valueEndec = valueEndec;
            this.maxSize = maxSize;

            return this;
        }

        @Override
        public int size() {
            return maxSize;
        }

        @Override
        public boolean hasNext() {
            return index < maxSize;
        }

        @Override
        public V next() {
            index++;

            return field("", valueEndec);
        }

        @Override
        public <F> F field(@Nullable String field, Endec<F> endec, @Nullable F defaultValue) {
            return endec.decode(DataInputDeserializer.this);
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final int maxSize;
        private final Endec<V> valueEndec;

        private int index = 0;

        private Map(Endec<V> valueEndec, int maxSize) {
            this.valueEndec = valueEndec;
            this.maxSize = maxSize;
        }

        @Override
        public int size() {
            return maxSize;
        }

        @Override
        public boolean hasNext() {
            return index < maxSize;
        }

        @Override
        public java.util.Map.Entry<String, V> next() {
            index++;

            return java.util.Map.entry(
                    DataInputDeserializer.this.readString(),
                    valueEndec.decode(DataInputDeserializer.this)
            );
        }
    }
}
