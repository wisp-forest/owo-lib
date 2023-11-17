package io.wispforest.owo.serialization.impl.bytebuf;

import io.netty.buffer.ByteBuf;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;

import java.util.Optional;
import java.util.Set;

public class ByteBufSerializer<T extends ByteBuf> implements Serializer<T> {

    private final T buf;

    public ByteBufSerializer(T buf){
        this.buf = (T) buf;
    }

    public static ByteBufSerializer<PacketByteBuf> packet(){
        return new ByteBufSerializer<>(PacketByteBufs.create());
    }

    //--

    @Override
    public Set<SerializationAttribute> attributes() {
        return Set.of();
    }

    //--

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        writeBoolean(optional.isPresent());

        optional.ifPresent(v -> endec.encode(this, v));
    }

    @Override
    public void writeBoolean(boolean value) {
        this.buf.writeBoolean(value);
    }

    @Override
    public void writeByte(byte value) {
        this.buf.writeByte(value);
    }

    @Override
    public void writeShort(short value) {
        this.buf.writeShort(value);
    }

    @Override
    public void writeInt(int value) {
        this.buf.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
        this.buf.writeLong(value);
    }

    @Override
    public void writeFloat(float value) {
        this.buf.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
        this.buf.writeDouble(value);
    }

    @Override
    public void writeString(String value) {
        StringEncoding.encode(buf, value, PacketByteBuf.DEFAULT_MAX_STRING_LENGTH);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        writeVarInt(bytes.length);
        this.buf.writeBytes(bytes);
    }

    @Override
    public void writeVarInt(int value) {
        VarInts.write(buf, value);
    }

    @Override
    public void writeVarLong(long value) {
        VarLongs.write(buf, value);
    }

    @Override
    public <V> Map<V> map(Endec<V> valueEndec, int size) {
        return (Map<V>) sequence(valueEndec, size);
    }

    @Override
    public <E> Serializer.Sequence<E> sequence(Endec<E> elementEndec, int size) {
        writeVarInt(size);

        return new Sequence<>(elementEndec);
    }

    @Override
    public Struct struct() {
        return new Sequence(null);
    }

    @Override
    public T result() {
        return (T) buf;
    }

    private class Sequence<V> implements Serializer.Sequence<V>, Struct, Map<V> {

        private final Endec<V> valueEndec;

        private Sequence(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;
        }

        @Override
        public void element(V element) {
            field("", valueEndec, element);
        }

        @Override
        public void entry(String key, V value) {
            ByteBufSerializer.this.writeString(key);
            field(key, valueEndec, value);
        }

        @Override
        public <F> Struct field(String name, Endec<F> endec, F value) {
            endec.encode(ByteBufSerializer.this, value);

            return this;
        }

        @Override public void end() {}

    }
}
