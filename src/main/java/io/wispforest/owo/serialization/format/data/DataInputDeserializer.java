package io.wispforest.owo.serialization.format.data;

import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationAttribute;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class DataInputDeserializer<D extends DataInput> implements Deserializer<D> {

    protected final D input;

    public DataInputDeserializer(D input) {
        this.input = input;
    }

    @Override
    public Set<SerializationAttribute> attributes() {
        return Set.of();
    }

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        return this.readBoolean()
                ? Optional.of(endec.decode(this))
                : Optional.empty();
    }

    @Override
    public boolean readBoolean() {
        try {
            return this.input.readBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte readByte() {
        try {
            return this.input.readByte();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short readShort() {
        try {
            return this.input.readShort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readInt() {
        try {
            return this.input.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readLong() {
        try {
            return this.input.readLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float readFloat() {
        try {
            return this.input.readFloat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double readDouble() {
        try {
            return this.input.readDouble();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readString() {
        try {
            return this.input.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] readBytes() {
        var result = new byte[this.readVarInt()];

        try {
            this.input.readFully(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public int readVarInt() {
        try {
            int result = 0;
            int bytes = 0;

            byte current;
            do {
                current = this.input.readByte();
                result |= (current & 127) << bytes++ * 7;
                if (bytes > 5) {
                    throw new RuntimeException("VarInt too big");
                }
            } while ((current & 128) == 128);

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readVarLong() {
        try {
            long result = 0L;
            int bytes = 0;

            byte current;
            do {
                current = this.input.readByte();
                result |= (long) (current & 127) << bytes++ * 7;
                if (bytes > 10) {
                    throw new RuntimeException("VarLong too big");
                }
            } while ((current & 128) == 128);

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <V> V tryRead(Function<Deserializer<D>, V> reader) {
        throw new UnsupportedOperationException("As DataInput cannot be rewound, tryRead(...) cannot be supported");
    }

    @Override
    public <E> Deserializer.Sequence<E> sequence(Endec<E> elementEndec) {
        return new Sequence<>(elementEndec, this.readVarInt());
    }

    @Override
    public <V> Deserializer.Map<V> map(Endec<V> valueEndec) {
        return new Map<>(valueEndec, this.readVarInt());
    }

    @Override
    public Struct struct() {
        return new Sequence<>(null, 0);
    }

    private class Sequence<V> implements Deserializer.Sequence<V>, Struct {

        private final Endec<V> valueEndec;
        private final int size;

        private int index = 0;

        private Sequence(Endec<V> valueEndec, int size) {
            this.valueEndec = valueEndec;
            this.size = size;
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.size;
        }

        @Override
        public V next() {
            this.index++;
            return this.valueEndec.decode(DataInputDeserializer.this);
        }

        @Override
        public <F> @Nullable F field(String name, Endec<F> endec) {
            return endec.decode(DataInputDeserializer.this);
        }

        @Override
        public <F> @Nullable F field(@Nullable String field, Endec<F> endec, @Nullable F defaultValue) {
            return endec.decode(DataInputDeserializer.this);
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final Endec<V> valueEndec;
        private final int size;

        private int index = 0;

        private Map(Endec<V> valueEndec, int size) {
            this.valueEndec = valueEndec;
            this.size = size;
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.size;
        }

        @Override
        public java.util.Map.Entry<String, V> next() {
            this.index++;
            return java.util.Map.entry(
                    DataInputDeserializer.this.readString(),
                    this.valueEndec.decode(DataInputDeserializer.this)
            );
        }
    }
}
