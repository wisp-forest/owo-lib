package io.wispforest.owo.serialization.format.bytebuf;

import io.netty.buffer.ByteBuf;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationContext;
import io.wispforest.owo.serialization.Serializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;

import java.util.Optional;

public class ByteBufSerializer<B extends ByteBuf> implements Serializer<B> {

    private final B buffer;

    protected ByteBufSerializer(B buffer) {
        this.buffer = buffer;
    }

    public static <B extends ByteBuf> ByteBufSerializer<B> of(B buffer) {
        return new ByteBufSerializer<>(buffer);
    }

    public static ByteBufSerializer<PacketByteBuf> packet() {
        return of(PacketByteBufs.create());
    }

    // ---

    @Override
    public void writeByte(SerializationContext ctx, byte value) {
        this.buffer.writeByte(value);
    }

    @Override
    public void writeShort(SerializationContext ctx, short value) {
        this.buffer.writeShort(value);
    }

    @Override
    public void writeInt(SerializationContext ctx, int value) {
        this.buffer.writeInt(value);
    }

    @Override
    public void writeLong(SerializationContext ctx, long value) {
        this.buffer.writeLong(value);
    }

    @Override
    public void writeFloat(SerializationContext ctx, float value) {
        this.buffer.writeFloat(value);
    }

    @Override
    public void writeDouble(SerializationContext ctx, double value) {
        this.buffer.writeDouble(value);
    }

    // ---

    @Override
    public void writeVarInt(SerializationContext ctx, int value) {
        VarInts.write(buffer, value);
    }

    @Override
    public void writeVarLong(SerializationContext ctx, long value) {
        VarLongs.write(buffer, value);
    }

    // ---

    @Override
    public void writeBoolean(SerializationContext ctx, boolean value) {
        this.buffer.writeBoolean(value);
    }

    @Override
    public void writeString(SerializationContext ctx, String value) {
        StringEncoding.encode(this.buffer, value, PacketByteBuf.DEFAULT_MAX_STRING_LENGTH);
    }

    @Override
    public void writeBytes(SerializationContext ctx, byte[] bytes) {
        this.writeVarInt(ctx, bytes.length);
        this.buffer.writeBytes(bytes);
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
    public B result() {
        return this.buffer;
    }

    // ---

    private class Sequence<V> implements Serializer.Sequence<V>, Struct, Map<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;
        }

        @Override
        public void element(V element) {
            this.valueEndec.encode(this.ctx, ByteBufSerializer.this, element);
        }

        @Override
        public void entry(String key, V value) {
            ByteBufSerializer.this.writeString(this.ctx, key);
            this.valueEndec.encode(this.ctx, ByteBufSerializer.this, value);
        }

        @Override
        public <F> Struct field(String name, SerializationContext ctx, Endec<F> endec, F value) {
            endec.encode(ctx, ByteBufSerializer.this, value);
            return this;
        }

        @Override
        public void end() {}
    }
}
