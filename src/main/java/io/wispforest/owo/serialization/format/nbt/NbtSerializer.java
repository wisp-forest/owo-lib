package io.wispforest.owo.serialization.format.nbt;

import com.google.common.collect.MapMaker;
import io.wispforest.endec.*;
import io.wispforest.endec.temp.OptionalFieldFlag;
import io.wispforest.endec.util.RecursiveSerializer;
import net.minecraft.nbt.*;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mutable;

import java.lang.ref.WeakReference;
import java.util.*;

public class NbtSerializer extends RecursiveSerializer<NbtElement> implements SelfDescribedSerializer<NbtElement> {

    protected NbtElement prefix;

    protected NbtSerializer(NbtElement prefix) {
        super(NbtEnd.INSTANCE);
        this.prefix = prefix;
    }

    public static NbtSerializer of(NbtElement prefix) {
        return new NbtSerializer(prefix);
    }

    public static NbtSerializer of() {
        return of(null);
    }

    // ---

    @Override
    public void writeByte(SerializationContext ctx, byte value) {
        this.consume(NbtByte.of(value));
    }

    @Override
    public void writeShort(SerializationContext ctx, short value) {
        this.consume(NbtShort.of(value));
    }

    @Override
    public void writeInt(SerializationContext ctx, int value) {
        this.consume(NbtInt.of(value));
    }

    @Override
    public void writeLong(SerializationContext ctx, long value) {
        this.consume(NbtLong.of(value));
    }

    @Override
    public void writeFloat(SerializationContext ctx, float value) {
        this.consume(NbtFloat.of(value));
    }

    @Override
    public void writeDouble(SerializationContext ctx, double value) {
        this.consume(NbtDouble.of(value));
    }

    // ---

    @Override
    public void writeVarInt(SerializationContext ctx, int value) {
        this.consume(switch (VarInts.getSizeInBytes(value)) {
            case 0, 1 -> NbtByte.of((byte) value);
            case 2 -> NbtShort.of((short) value);
            default -> NbtInt.of(value);
        });
    }

    @Override
    public void writeVarLong(SerializationContext ctx, long value) {
        this.consume(switch (VarLongs.getSizeInBytes(value)) {
            case 0, 1 -> NbtByte.of((byte) value);
            case 2 -> NbtShort.of((short) value);
            case 3, 4 -> NbtInt.of((int) value);
            default -> NbtLong.of(value);
        });
    }

    // ---

    @Override
    public void writeBoolean(SerializationContext ctx, boolean value) {
        this.consume(NbtByte.of(value));
    }

    @Override
    public void writeString(SerializationContext ctx, String value) {
        this.consume(NbtString.of(value));
    }

    @Override
    public void writeBytes(SerializationContext ctx, byte[] bytes) {
        this.consume(new NbtByteArray(bytes));
    }

    private final Set<NbtElement> encodedOptionals = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    @Override
    public <V> void writeOptional(SerializationContext ctx, Endec<V> endec, Optional<V> optional) {
        MutableObject<NbtElement> frameData = new MutableObject<>();

        this.frame(encoded -> {
            try (var struct = this.struct()) {
                struct.field("present", ctx, Endec.BOOLEAN, optional.isPresent());
                optional.ifPresent(value -> struct.field("value", ctx.withoutAttributes(OptionalFieldFlag.INSTANCE), endec, value));
            }

            var compound = encoded.require("optional representation");

            encodedOptionals.add(compound);
            frameData.setValue(compound);
        }, false);

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
        private final NbtCompound result;

        private Map(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            if (NbtSerializer.this.prefix != null) {
                if (NbtSerializer.this.prefix instanceof NbtCompound prefixMap) {
                    this.result = prefixMap;
                } else {
                    throw new IllegalStateException("Incompatible prefix of type " + NbtSerializer.this.prefix.getClass().getSimpleName() + " provided for NBT map/struct");
                }
            } else {
                this.result = new NbtCompound();
            }
        }

        @Override
        public void entry(String key, V value) {
            NbtSerializer.this.frame(encoded -> {
                this.valueEndec.encode(this.ctx, NbtSerializer.this, value);
                this.result.put(key, encoded.require("map value"));
            }, false);
        }

        @Override
        public <F> Struct field(String name, SerializationContext ctx, Endec<F> endec, F value) {
            boolean mayOmit = ctx.hasAttribute(OptionalFieldFlag.INSTANCE);

            NbtSerializer.this.frame(encoded -> {
                endec.encode(ctx, NbtSerializer.this, value);

                var element = encoded.require("struct field");

                if (mayOmit && NbtSerializer.this.encodedOptionals.contains(element)) {
                    var nbtCompound = (NbtCompound) element;

                    if(!nbtCompound.getBoolean("present")) return;

                    element = nbtCompound.get("value");
                }

                this.result.put(name, element);
            }, false);

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
        private final NbtList result;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            if (NbtSerializer.this.prefix != null) {
                if (NbtSerializer.this.prefix instanceof NbtList prefixList) {
                    this.result = prefixList;
                } else {
                    throw new IllegalStateException("Incompatible prefix of type " + NbtSerializer.this.prefix.getClass().getSimpleName() + " provided for NBT sequence");
                }
            } else {
                this.result = new NbtList();
            }
        }

        @Override
        public void element(V element) {
            NbtSerializer.this.frame(encoded -> {
                this.valueEndec.encode(this.ctx, NbtSerializer.this, element);
                this.result.add(encoded.require("sequence element"));
            }, false);
        }

        @Override
        public void end() {
            var convertedResult = switch (this.result.getHeldType()) {
                case NbtElement.BYTE_TYPE -> {
                    var list = new ArrayList<Byte>();
                    for (var nbtElement : this.result) {
                        list.add(((AbstractNbtNumber) nbtElement).byteValue());
                    }
                    yield new NbtByteArray(list);
                }
                case NbtElement.INT_TYPE -> {
                    var list = new ArrayList<Integer>();
                    for (var nbtElement : this.result) {
                        list.add(((AbstractNbtNumber) nbtElement).intValue());
                    }
                    yield new NbtIntArray(list);
                }
                case NbtElement.LONG_TYPE -> {
                    var list = new ArrayList<Long>();
                    for (NbtElement nbtElement : this.result) {
                        list.add(((AbstractNbtNumber) nbtElement).longValue());
                    }
                    yield new NbtLongArray(list);
                }
                default -> this.result;
            };

            NbtSerializer.this.consume(convertedResult);
        }
    }
}
