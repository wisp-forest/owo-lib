package io.wispforest.owo.serialization.format.bytebuf;

import io.netty.buffer.ByteBuf;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationAttribute;
import io.wispforest.owo.serialization.Serializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;

import java.util.Optional;
import java.util.Set;

public class ByteBufSerializer<B extends ByteBuf> implements Serializer<B> {

    private final B buffer;

    public ByteBufSerializer(B buffer) {
        this.buffer = buffer;
    }

    public static ByteBufSerializer<PacketByteBuf> packet() {
        return new ByteBufSerializer<>(PacketByteBufs.create());
    }

    // ---

    @Override
    public Set<SerializationAttribute> attributes() {
        return Set.of();
    }

    // ---

    @Override
    public void writeByte(byte value) {
        this.buffer.writeByte(value);
    }

    @Override
    public void writeShort(short value) {
        this.buffer.writeShort(value);
    }

    @Override
    public void writeInt(int value) {
        this.buffer.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
        this.buffer.writeLong(value);
    }

    @Override
    public void writeFloat(float value) {
        this.buffer.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
        this.buffer.writeDouble(value);
    }

    // ---

    @Override
    public void writeVarInt(int value) {
        VarInts.write(buffer, value);
    }

    @Override
    public void writeVarLong(long value) {
        VarLongs.write(buffer, value);
    }

    // ---

    @Override
    public void writeBoolean(boolean value) {
        this.buffer.writeBoolean(value);
    }

    @Override
    public void writeString(String value) {
        StringEncoding.encode(this.buffer, value, PacketByteBuf.DEFAULT_MAX_STRING_LENGTH);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        this.writeVarInt(bytes.length);
        this.buffer.writeBytes(bytes);
    }

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        this.writeBoolean(optional.isPresent());
        optional.ifPresent(value -> endec.encode(this, value));
    }

    // ---

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

    // ---

    @Override
    public B result() {
        return this.buffer;
    }

    // ---

    private class Sequence<V> implements Serializer.Sequence<V>, Struct, Map<V> {

        private final Endec<V> valueEndec;

        private Sequence(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;
        }

        @Override
        public void element(V element) {
            this.valueEndec.encode(ByteBufSerializer.this, element);
        }

        @Override
        public void entry(String key, V value) {
            ByteBufSerializer.this.writeString(key);
            this.valueEndec.encode(ByteBufSerializer.this, value);
        }

        @Override
        public <F> Struct field(String name, Endec<F> endec, F value) {
            endec.encode(ByteBufSerializer.this, value);
            return this;
        }

        @Override
        public void end() {}
    }
}
