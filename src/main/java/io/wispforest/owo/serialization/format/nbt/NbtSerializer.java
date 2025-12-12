package io.wispforest.owo.serialization.format.nbt;

import com.google.common.collect.MapMaker;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SelfDescribedSerializer;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.Serializer;
import io.wispforest.endec.util.RecursiveSerializer;
import net.minecraft.nbt.*;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class NbtSerializer extends RecursiveSerializer<Tag> implements SelfDescribedSerializer<Tag> {

    protected Tag prefix;

    protected NbtSerializer(Tag prefix) {
        super(EndTag.INSTANCE);
        this.prefix = prefix;
    }

    public static NbtSerializer of(Tag prefix) {
        return new NbtSerializer(prefix);
    }

    public static NbtSerializer of() {
        return of(null);
    }

    // ---

    @Override
    public void writeByte(SerializationContext ctx, byte value) {
        this.consume(ByteTag.valueOf(value));
    }

    @Override
    public void writeShort(SerializationContext ctx, short value) {
        this.consume(ShortTag.valueOf(value));
    }

    @Override
    public void writeInt(SerializationContext ctx, int value) {
        this.consume(IntTag.valueOf(value));
    }

    @Override
    public void writeLong(SerializationContext ctx, long value) {
        this.consume(LongTag.valueOf(value));
    }

    @Override
    public void writeFloat(SerializationContext ctx, float value) {
        this.consume(FloatTag.valueOf(value));
    }

    @Override
    public void writeDouble(SerializationContext ctx, double value) {
        this.consume(DoubleTag.valueOf(value));
    }

    // ---

    @Override
    public void writeVarInt(SerializationContext ctx, int value) {
        this.consume(switch (VarInt.getByteSize(value)) {
            case 0, 1 -> ByteTag.valueOf((byte) value);
            case 2 -> ShortTag.valueOf((short) value);
            default -> IntTag.valueOf(value);
        });
    }

    @Override
    public void writeVarLong(SerializationContext ctx, long value) {
        this.consume(switch (VarLong.getByteSize(value)) {
            case 0, 1 -> ByteTag.valueOf((byte) value);
            case 2 -> ShortTag.valueOf((short) value);
            case 3, 4 -> IntTag.valueOf((int) value);
            default -> LongTag.valueOf(value);
        });
    }

    // ---

    @Override
    public void writeBoolean(SerializationContext ctx, boolean value) {
        this.consume(ByteTag.valueOf(value));
    }

    @Override
    public void writeString(SerializationContext ctx, String value) {
        this.consume(StringTag.valueOf(value));
    }

    @Override
    public void writeBytes(SerializationContext ctx, byte[] bytes) {
        this.consume(new ByteArrayTag(bytes));
    }

    private final Set<Tag> encodedOptionals = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    @Override
    public <V> void writeOptional(SerializationContext ctx, Endec<V> endec, Optional<V> optional) {
        MutableObject<Tag> frameData = new MutableObject<>();

        this.frame(encoded -> {
            try (var struct = this.struct()) {
                struct.field("present", ctx, Endec.BOOLEAN, optional.isPresent());
                optional.ifPresent(value -> struct.field("value", ctx, endec, value));
            }

            var compound = encoded.require("optional representation");

            encodedOptionals.add(compound);
            frameData.setValue(compound);
        });

        this.consume(frameData.getValue());
    }

    // ---

    @Override
    public <E> Serializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec, int size) {
        return new Sequence<>(ctx, elementEndec);
    }

    @Override
    public <V> Serializer.Map<V> map(SerializationContext ctx, Endec<V> valueEndec, int size) {
        return new Map<>(ctx, valueEndec);
    }

    @Override
    public Struct struct() {
        return new Map<>(null, null);
    }

    // ---

    private class Map<V> implements Serializer.Map<V>, Struct {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final CompoundTag result;

        private Map(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            if (NbtSerializer.this.prefix != null) {
                if (NbtSerializer.this.prefix instanceof CompoundTag prefixMap) {
                    this.result = prefixMap;
                } else {
                    throw new IllegalStateException("Incompatible prefix of type " + NbtSerializer.this.prefix.getClass().getSimpleName() + " provided for NBT map/struct");
                }
            } else {
                this.result = new CompoundTag();
            }
        }

        @Override
        public void entry(String key, V value) {
            NbtSerializer.this.frame(encoded -> {
                this.valueEndec.encode(this.ctx, NbtSerializer.this, value);
                this.result.put(key, encoded.require("map value"));
            });
        }

        @Override
        public <F> Struct field(String name, SerializationContext ctx, Endec<F> endec, F value, boolean mayOmit) {
            NbtSerializer.this.frame(encoded -> {
                endec.encode(ctx, NbtSerializer.this, value);

                var element = encoded.require("struct field");

                if (mayOmit && NbtSerializer.this.encodedOptionals.contains(element)) {
                    var nbtCompound = (CompoundTag) element;

                    if(!nbtCompound.getBooleanOr("present", false)) return;

                    element = nbtCompound.get("value");
                }

                this.result.put(name, element);
            });

            return this;
        }

        @Override
        public void end() {
            NbtSerializer.this.consume(this.result);
        }
    }

    private class Sequence<V> implements Serializer.Sequence<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final ListTag result;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            if (NbtSerializer.this.prefix != null) {
                if (NbtSerializer.this.prefix instanceof ListTag prefixList) {
                    this.result = prefixList;
                } else {
                    throw new IllegalStateException("Incompatible prefix of type " + NbtSerializer.this.prefix.getClass().getSimpleName() + " provided for NBT sequence");
                }
            } else {
                this.result = new ListTag();
            }
        }

        @Override
        public void element(V element) {
            NbtSerializer.this.frame(encoded -> {
                this.valueEndec.encode(this.ctx, NbtSerializer.this, element);
                this.result.add(encoded.require("sequence element"));
            });
        }

        @Override
        public void end() {
            NbtSerializer.this.consume(this.result);
        }
    }
}
