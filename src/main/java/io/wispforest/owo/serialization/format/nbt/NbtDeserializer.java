package io.wispforest.owo.serialization.format.nbt;

import com.google.common.collect.MapMaker;
import io.wispforest.endec.*;
import io.wispforest.endec.format.edm.EdmElement;
import io.wispforest.endec.util.RecursiveDeserializer;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class NbtDeserializer extends RecursiveDeserializer<Tag> implements SelfDescribedDeserializer<Tag> {

    protected NbtDeserializer(Tag element) {
        super(element);
    }

    public static NbtDeserializer of(Tag element) {
        return new NbtDeserializer(element);
    }

    private <N extends Tag> N getAs(SerializationContext ctx, Tag element, Class<N> clazz) {
        if (!clazz.isInstance(element)) {
            ctx.throwMalformedInput("Expected a " + clazz.getSimpleName() + ", found a " + element.getClass().getSimpleName());
        }

        return clazz.cast(element);
    }

    // ---

    @Override
    public byte readByte(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), ByteTag.class).byteValue();
    }

    @Override
    public short readShort(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), ShortTag.class).shortValue();
    }

    @Override
    public int readInt(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), IntTag.class).intValue();
    }

    @Override
    public long readLong(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), LongTag.class).longValue();
    }

    @Override
    public float readFloat(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), FloatTag.class).floatValue();
    }

    @Override
    public double readDouble(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), DoubleTag.class).doubleValue();
    }

    // ---

    @Override
    public int readVarInt(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), NumericTag.class).intValue();
    }

    @Override
    public long readVarLong(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), NumericTag.class).longValue();
    }

    // ---

    @Override
    public boolean readBoolean(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), ByteTag.class).byteValue() != 0;
    }

    @Override
    public String readString(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), StringTag.class).asString().get();
    }

    @Override
    public byte[] readBytes(SerializationContext ctx) {
        return this.getAs(ctx, this.getValue(), ByteArrayTag.class).getAsByteArray();
    }

    private final Set<Tag> encodedOptionals = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    @Override
    public <V> Optional<V> readOptional(SerializationContext ctx, Endec<V> endec) {
        var value = this.getValue();
        if (this.encodedOptionals.contains(value)) {
            return Optional.of(endec.decode(ctx, this));
        }

        var struct = this.struct(ctx);
        return struct.field("present", ctx, Endec.BOOLEAN)
                ? Optional.of(struct.field("value", ctx, endec))
                : Optional.empty();
    }

    // ---

    @Override
    public <E> Deserializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec) {
        //noinspection unchecked
        var list = this.getAs(ctx, this.getValue(), CollectionTag.class);
        return new Sequence<E>(ctx, elementEndec, list, list.size());
    }

    @Override
    public <V> Deserializer.Map<V> map(SerializationContext ctx, Endec<V> valueEndec) {
        return new Map<>(ctx, valueEndec, this.getAs(ctx, this.getValue(), CompoundTag.class));
    }

    @Override
    public Deserializer.Struct struct(SerializationContext ctx) {
        return new Struct(this.getAs(ctx, this.getValue(), CompoundTag.class));
    }

    // ---

    @Override
    public <S> void readAny(SerializationContext ctx, Serializer<S> visitor) {
        this.decodeValue(ctx, visitor, this.getValue());
    }

    private <S> void decodeValue(SerializationContext ctx, Serializer<S> visitor, Tag value) {
        switch (value.getId()) {
            case Tag.TAG_BYTE -> visitor.writeByte(ctx, ((ByteTag) value).byteValue());
            case Tag.TAG_SHORT -> visitor.writeShort(ctx, ((ShortTag) value).shortValue());
            case Tag.TAG_INT -> visitor.writeInt(ctx, ((IntTag) value).intValue());
            case Tag.TAG_LONG -> visitor.writeLong(ctx, ((LongTag) value).longValue());
            case Tag.TAG_FLOAT -> visitor.writeFloat(ctx, ((FloatTag) value).floatValue());
            case Tag.TAG_DOUBLE -> visitor.writeDouble(ctx, ((DoubleTag) value).doubleValue());
            case Tag.TAG_STRING -> visitor.writeString(ctx, value.asString().get());
            case Tag.TAG_BYTE_ARRAY -> visitor.writeBytes(ctx, ((ByteArrayTag) value).getAsByteArray());
            case Tag.TAG_INT_ARRAY, Tag.TAG_LONG_ARRAY, Tag.TAG_LIST -> {
                var list = (CollectionTag) value;
                try (var sequence = visitor.sequence(ctx, Endec.<Tag>of(this::decodeValue, (ctx1, deserializer) -> null), list.size())) {
                    list.forEach(sequence::element);
                }
            }
            case Tag.TAG_COMPOUND -> {
                var compound = (CompoundTag) value;
                try (var map = visitor.map(ctx, Endec.<Tag>of(this::decodeValue, (ctx1, deserializer) -> null), compound.size())) {
                    for (var key : compound.keySet()) {
                        map.entry(key, compound.get(key));
                    }
                }
            }
            default ->
                    throw new IllegalArgumentException("Non-standard, unrecognized NbtElement implementation cannot be decoded");
        }
    }

    // ---

    private class Sequence<V> implements Deserializer.Sequence<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final Iterator<Tag> elements;
        private final int size;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec, Iterable<Tag> elements, int size) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            this.elements = elements.iterator();
            this.size = size;
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.elements.hasNext();
        }

        @Override
        public V next() {
            var value = this.elements.next();

            return NbtDeserializer.this.frame(
                    () -> value,
                    () -> this.valueEndec.decode(this.ctx, NbtDeserializer.this)
            );
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final CompoundTag compound;
        private final Iterator<String> keys;
        private final int size;

        private Map(SerializationContext ctx, Endec<V> valueEndec, CompoundTag compound) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            this.compound = compound;
            this.keys = compound.keySet().iterator();
            this.size = compound.size();
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.keys.hasNext();
        }

        @Override
        public java.util.Map.Entry<String, V> next() {
            var key = this.keys.next();
            return NbtDeserializer.this.frame(
                    () -> this.compound.get(key),
                    () -> java.util.Map.entry(key, this.valueEndec.decode(this.ctx, NbtDeserializer.this))
            );
        }
    }

    public class Struct implements Deserializer.Struct {

        private final CompoundTag compound;

        public Struct(CompoundTag compound) {
            this.compound = compound;
        }

        @Override
        public <F> @Nullable F field(String name, SerializationContext ctx, Endec<F> endec, @Nullable Supplier<F> defaultValueFactory) {
            if (!this.compound.contains(name)) {
                if (defaultValueFactory == null) {
                    throw new IllegalStateException("Field '" + name + "' was missing from serialized data, but no default value was provided");
                }

                return defaultValueFactory.get();
            }
            var element = this.compound.get(name);
            if (defaultValueFactory != null) NbtDeserializer.this.encodedOptionals.add(element);
            return NbtDeserializer.this.frame(
                    () -> element,
                    () -> endec.decode(ctx, NbtDeserializer.this)
            );
        }
    }
}
