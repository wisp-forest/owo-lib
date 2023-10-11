package io.wispforest.owo.serialization.impl.bytebuf;

import io.netty.buffer.ByteBuf;
import io.wispforest.owo.serialization.*;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public class ByteBufDeserializer implements Deserializer<ByteBuf> {

    private final ByteBuf buf;

    public ByteBufDeserializer(ByteBuf buf){
        this.buf = buf;
    }

    @Override
    public <V> Optional<V> readOptional(Codeck<V> codeck) {
        var bl = buf.readBoolean();

        return Optional.ofNullable(bl ? codeck.decode(this) : null);
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    @Override
    public String readString() {
        return buf.toString(Charset.defaultCharset());
    }

    @Override
    public byte[] readBytes() {
        var array = new byte[buf.readInt()];

        buf.readBytes(array);

        return array;
    }

    @Override
    public <E> SequenceDeserializer<E> sequence(Codeck<E> elementCodec) {
        return new ByteBufSequenceDeserializer<E>().valueCodec(elementCodec, buf.readInt());
    }

    @Override
    public <V> MapDeserializer<V> map(Codeck<V> valueCodec) {
        return new ByteBufMapDeserializer<V>(valueCodec, buf.readInt());
    }

    @Override
    public StructDeserializer struct() {
        return new ByteBufSequenceDeserializer<>();
    }

    public class ByteBufSequenceDeserializer<V> implements SequenceDeserializer<V>, StructDeserializer {

        private int maxSize;
        private Codeck<V> valueCodec;

        private int index = 0;

        public ByteBufSequenceDeserializer<V> valueCodec(Codeck<V> valueCodec, int maxSize) {
            this.valueCodec = valueCodec;
            this.maxSize = maxSize;

            return this;
        }

        @Override
        public boolean hasNext() {
            return index < maxSize;
        }

        @Override
        public V next() {
            index++;

            return field("", valueCodec);
        }

        @Override
        public <F> F field(@Nullable String field, Codeck<F> codecy, @Nullable F defaultValue) {
            return codecy.decode(ByteBufDeserializer.this);
        }
    }

    public class ByteBufMapDeserializer<V> implements MapDeserializer<V> {

        private final int maxSize;
        private final Codeck<V> valueCodec;

        private int index = 0;

        public ByteBufMapDeserializer(Codeck<V> valueCodec, int maxSize) {
            this.valueCodec = valueCodec;
            this.maxSize = maxSize;
        }

        @Override
        public boolean hasNext() {
            return index < maxSize;
        }

        @Override
        public Map.Entry<String, V> next() {
            index++;

            return Map.entry(
                    ByteBufDeserializer.this.readString(),
                    valueCodec.decode(ByteBufDeserializer.this)
            );
        }
    }

}
