package io.wispforest.owo.serialization.format.nbt;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.util.RecursiveDeserializer;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NbtDeserializer extends RecursiveDeserializer<NbtElement> implements SelfDescribedDeserializer<NbtElement> {

    protected NbtDeserializer(NbtElement element) {
        super(element);
    }

    public static NbtDeserializer of(NbtElement element) {
        return new NbtDeserializer(element);
    }

    private <N extends NbtElement> N getAs(NbtElement element, Class<N> clazz) {
        if (clazz.isInstance(element)) {
            return clazz.cast(element);
        } else {
            throw new IllegalStateException("Expected a " + clazz.getSimpleName() + ", found a " + element.getClass().getSimpleName());
        }
    }

    // ---

    @Override
    public boolean hasAttribute(SerializationAttribute attribute) {
        return attribute == SerializationAttributes.SELF_DESCRIBING;
    }

    @Override
    public <A> A getAttributeValue(SerializationAttribute.WithValue<A> attribute) {
        throw new IllegalArgumentException("NbtDeserializer does not provide any attribute values");
    }

    // ---

    @Override
    public byte readByte() {
        return this.getAs(this.getValue(), NbtByte.class).byteValue();
    }

    @Override
    public short readShort() {
        return this.getAs(this.getValue(), NbtShort.class).shortValue();
    }

    @Override
    public int readInt() {
        return this.getAs(this.getValue(), NbtInt.class).intValue();
    }

    @Override
    public long readLong() {
        return this.getAs(this.getValue(), NbtLong.class).longValue();
    }

    @Override
    public float readFloat() {
        return this.getAs(this.getValue(), NbtFloat.class).floatValue();
    }

    @Override
    public double readDouble() {
        return this.getAs(this.getValue(), NbtDouble.class).doubleValue();
    }

    // ---

    @Override
    public int readVarInt() {
        return this.getAs(this.getValue(), AbstractNbtNumber.class).intValue();
    }

    @Override
    public long readVarLong() {
        return this.getAs(this.getValue(), AbstractNbtNumber.class).longValue();
    }

    // ---

    @Override
    public boolean readBoolean() {
        return this.getAs(this.getValue(), NbtByte.class).byteValue() != 0;
    }

    @Override
    public String readString() {
        return this.getAs(this.getValue(), NbtString.class).asString();
    }

    @Override
    public byte[] readBytes() {
        return this.getAs(this.getValue(), NbtByteArray.class).getByteArray();
    }

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        if (this.isReadingStructField()) {
            return Optional.of(endec.decode(this));
        } else {
            var struct = this.struct();
            return struct.field("present", Endec.BOOLEAN)
                    ? Optional.of(struct.field("value", endec))
                    : Optional.empty();
        }
    }

    // ---

    @Override
    public <E> Deserializer.Sequence<E> sequence(Endec<E> elementEndec) {
        //noinspection unchecked
        return new Sequence<>(elementEndec, this.getAs(this.getValue(), AbstractNbtList.class));
    }

    @Override
    public <V> Deserializer.Map<V> map(Endec<V> valueEndec) {
        return new Map<>(valueEndec, this.getAs(this.getValue(), NbtCompound.class));
    }

    @Override
    public Deserializer.Struct struct() {
        return new Struct(this.getAs(this.getValue(), NbtCompound.class));
    }

    // ---

    @Override
    public <S> void readAny(Serializer<S> visitor) {
        this.decodeValue(visitor, this.getValue());
    }

    private <S> void decodeValue(Serializer<S> visitor, NbtElement value) {
        switch (value.getType()) {
            case NbtElement.BYTE_TYPE -> visitor.writeByte(((NbtByte) value).byteValue());
            case NbtElement.SHORT_TYPE -> visitor.writeShort(((NbtShort) value).shortValue());
            case NbtElement.INT_TYPE -> visitor.writeInt(((NbtInt) value).intValue());
            case NbtElement.LONG_TYPE -> visitor.writeLong(((NbtLong) value).longValue());
            case NbtElement.FLOAT_TYPE -> visitor.writeFloat(((NbtFloat) value).floatValue());
            case NbtElement.DOUBLE_TYPE -> visitor.writeDouble(((NbtDouble) value).doubleValue());
            case NbtElement.STRING_TYPE -> visitor.writeString(value.asString());
            case NbtElement.BYTE_ARRAY_TYPE -> visitor.writeBytes(((NbtByteArray) value).getByteArray());
            case NbtElement.INT_ARRAY_TYPE, NbtElement.LONG_ARRAY_TYPE, NbtElement.LIST_TYPE -> {
                var list = (AbstractNbtList<?>) value;
                try (var sequence = visitor.sequence(Endec.<NbtElement>of(this::decodeValue, deserializer -> null), list.size())) {
                    list.forEach(sequence::element);
                }
            }
            case NbtElement.COMPOUND_TYPE -> {
                var compound = (NbtCompound) value;
                try (var map = visitor.map(Endec.<NbtElement>of(this::decodeValue, deserializer -> null), compound.getSize())) {
                    for (var key : compound.getKeys()) {
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

        private final Endec<V> valueEndec;
        private final Iterator<NbtElement> elements;
        private final int size;

        private Sequence(Endec<V> valueEndec, List<NbtElement> elements) {
            this.valueEndec = valueEndec;

            this.elements = elements.iterator();
            this.size = elements.size();
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
            return NbtDeserializer.this.frame(
                    this.elements::next,
                    () -> this.valueEndec.decode(NbtDeserializer.this),
                    false
            );
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final Endec<V> valueEndec;
        private final NbtCompound compound;
        private final Iterator<String> keys;
        private final int size;

        private Map(Endec<V> valueEndec, NbtCompound compound) {
            this.valueEndec = valueEndec;

            this.compound = compound;
            this.keys = compound.getKeys().iterator();
            this.size = compound.getSize();
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
                    () -> java.util.Map.entry(key, this.valueEndec.decode(NbtDeserializer.this)),
                    false
            );
        }
    }

    public class Struct implements Deserializer.Struct {

        private final NbtCompound compound;

        public Struct(NbtCompound compound) {
            this.compound = compound;
        }

        @Override
        public <F> @Nullable F field(String name, Endec<F> endec) {
            if (!this.compound.contains(name)) {
                throw new IllegalStateException("Field '" + name + "' was missing from serialized data, but no default value was provided");
            }

            return NbtDeserializer.this.frame(
                    () -> this.compound.get(name),
                    () -> endec.decode(NbtDeserializer.this),
                    true
            );
        }

        @Override
        public <F> @Nullable F field(String name, Endec<F> endec, @Nullable F defaultValue) {
            if (!this.compound.contains(name)) return defaultValue;
            return NbtDeserializer.this.frame(
                    () -> this.compound.get(name),
                    () -> endec.decode(NbtDeserializer.this),
                    true
            );
        }
    }
}
