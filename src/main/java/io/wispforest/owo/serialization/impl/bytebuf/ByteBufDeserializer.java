package io.wispforest.owo.serialization.impl.bytebuf;

import io.netty.buffer.ByteBuf;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ByteBufDeserializer implements Deserializer<ByteBuf> {

    private final ByteBuf buf;

    public ByteBufDeserializer(ByteBuf buf){
        this.buf = buf;
    }

    //--

    @Override
    public Set<SerializationAttribute> attributes() {
        return Set.of(SerializationAttribute.BINARY);
    }

    //--

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
        return StringEncoding.decode(buf, PacketByteBuf.DEFAULT_MAX_STRING_LENGTH);
    }

    @Override
    public byte[] readBytes() {
        var array = new byte[readVarInt()];

        buf.readBytes(array);

        return array;
    }

    @Override
    public int readVarInt() {
        return VarInts.read(buf);
    }

    @Override
    public long readVarLong() {
        return VarLongs.read(buf);
    }

    @Override
    public <E> SequenceDeserializer<E> sequence(Codeck<E> elementCodec) {
        return new ByteBufSequenceDeserializer<E>().valueCodec(elementCodec, readVarInt());
    }

    @Override
    public <V> MapDeserializer<V> map(Codeck<V> valueCodec) {
        return new ByteBufMapDeserializer<V>(valueCodec, readVarInt());
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
