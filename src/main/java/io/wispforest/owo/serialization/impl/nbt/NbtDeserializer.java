package io.wispforest.owo.serialization.impl.nbt;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class NbtDeserializer implements SelfDescribedDeserializer<NbtElement> {

    private final SerializationAttribute extraAttribute;

    private final Deque<Supplier<NbtElement>> stack = new ArrayDeque<>();

    private boolean safeHandling = true;

    private NbtDeserializer(NbtElement element, SerializationAttribute attribute) {
        stack.push(() -> element);

        extraAttribute = attribute;
    }

    public NbtDeserializer safeHandling(boolean value) {
        this.safeHandling = value;

        return this;
    }

    private NbtElement topElement() {
        return stack.peek().get();
    }

    //--

    public static NbtDeserializer of(NbtElement element){
        return new NbtDeserializer(element, SerializationAttribute.HUMAN_READABLE);
    }

    @Override
    public Set<SerializationAttribute> attributes() {
        Set<SerializationAttribute> set = new HashSet<>();

        set.add(SerializationAttribute.SELF_DESCRIBING);
        set.add(extraAttribute);

        return set;
    }

    //--

    @Override
    public <S> void readAny(Serializer<S> visitor) {
        this.decodeValue(visitor, this.topElement());
    }

    private <S> void decodeValue(Serializer<S> visitor, NbtElement value)  {
        switch (value.getType()) {
            case NbtElement.BYTE_TYPE -> visitor.writeByte(((NbtByte)value).byteValue());
            case NbtElement.SHORT_TYPE -> visitor.writeShort(((NbtShort)value).shortValue());
            case NbtElement.INT_TYPE -> visitor.writeInt(((NbtInt)value).intValue());
            case NbtElement.LONG_TYPE -> visitor.writeLong(((NbtLong)value).longValue());
            case NbtElement.FLOAT_TYPE -> visitor.writeFloat(((NbtFloat)value).floatValue());
            case NbtElement.DOUBLE_TYPE -> visitor.writeDouble(((NbtDouble)value).doubleValue());
            case NbtElement.STRING_TYPE -> visitor.writeString(value.asString());
            case NbtElement.BYTE_ARRAY_TYPE -> visitor.writeBytes(((NbtByteArray)value).getByteArray());
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
            default -> throw new IllegalArgumentException("Non-standard, unrecognized NbtElement implementation cannot be decoded");
        }
    }

    //--

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        var struct = struct();

        var present = struct.field("present", Endec.BOOLEAN);

        return Optional.ofNullable(present ? struct.field("value", endec) : null);
    }

    @Override
    public boolean readBoolean() {
        if (topElement() instanceof NbtByte nbtNumber) return nbtNumber.byteValue() != 0;

        if (safeHandling) return false; // Default value: 0 != 0;

        throw new RuntimeException("[NbtFormat] input was not NbtByte for a Boolean get call");
    }

    @Override
    public byte readByte() {
        if (topElement() instanceof NbtByte nbtNumber) return nbtNumber.byteValue();

        if (safeHandling) return 0;

        throw new RuntimeException("[NbtFormat] input was not NbtByte for a Byte get call");
    }

    @Override
    public short readShort() {
        if (topElement() instanceof NbtShort nbtNumber) return nbtNumber.shortValue();

        if (safeHandling) return 0;

        throw new RuntimeException("[NbtFormat] input was not NbtShort");
    }

    @Override
    public int readInt() {
        if (topElement() instanceof NbtInt nbtNumber) return nbtNumber.intValue();

        if (safeHandling) return 0;

        throw new RuntimeException("[NbtFormat] input was not NbtInt");
    }

    @Override
    public long readLong() {
        if (topElement() instanceof NbtLong nbtNumber) return nbtNumber.longValue();

        if (safeHandling) return 0L;

        throw new RuntimeException("[NbtFormat] input was not NbtLong");
    }

    @Override
    public float readFloat() {
        if (topElement() instanceof NbtFloat nbtNumber) return nbtNumber.floatValue();

        if (safeHandling) return 0F;

        throw new RuntimeException("[NbtFormat] input was not NbtFloat");
    }

    @Override
    public double readDouble() {
        if (topElement() instanceof NbtDouble nbtNumber) return nbtNumber.doubleValue();

        if (safeHandling) return 0D;

        throw new RuntimeException("[NbtFormat] input was not NbtDouble");
    }

    @Override
    public String readString() {
        if (topElement() instanceof NbtString nbtString) return nbtString.asString();

        if (safeHandling) return "";

        throw new RuntimeException("[NbtFormat] input was not NbtDouble");
    }

    @Override
    public byte[] readBytes() {
        if(topElement() instanceof NbtByteArray nbtBytesArray) return nbtBytesArray.getByteArray();

        if (safeHandling) return new byte[0];

        throw new RuntimeException("[NbtFormat] input was not NbtByteArray");
    }

    @Override
    public int readVarInt() {
        if (topElement() instanceof AbstractNbtNumber nbtNumber) return nbtNumber.intValue();

        if (safeHandling) return 0;

        throw new RuntimeException("[NbtFormat] input was not AbstractNbtNumber");
    }

    @Override
    public long readVarLong() {
        if (topElement() instanceof AbstractNbtNumber nbtNumber) return nbtNumber.longValue();

        if (safeHandling) return 0;

        throw new RuntimeException("[NbtFormat] input was not AbstractNbtNumber");
    }

    @Override
    public <V> V tryRead(Function<Deserializer<NbtElement>, V> func) {
        var stackCopy = new ArrayList<>(stack);

        try {
            return func.apply(this);
        } catch (Exception e){
            stack.clear();
            stack.addAll(stackCopy);

            throw e;
        }
    }

    @Override
    public <E> SequenceDeserializer<E> sequence(Endec<E> elementEndec) {
        return new NbtSequenceDeserializer<>(((AbstractNbtList<NbtElement>) topElement()), elementEndec);
    }

    @Override
    public <V> MapDeserializer<V> map(Endec<V> valueEndec) {
        return new NbtMapDeserializer<>(((NbtCompound) topElement()).toMap(), valueEndec);
    }

    @Override
    public StructDeserializer struct() {
        return new NbtStructDeserializer(((NbtCompound) topElement()).toMap());
    }

    public class NbtSequenceDeserializer<V> implements SequenceDeserializer<V> {

        private final Iterator<NbtElement> entries;
        private final int maxSize;

        private final Endec<V> valueEndec;

        public NbtSequenceDeserializer(List<NbtElement> entries, Endec<V> valueEndec) {
            this.entries = entries.iterator();
            this.maxSize = entries.size();

            this.valueEndec = valueEndec;
        }

        @Override
        public int size() {
            return maxSize;
        }

        @Override
        public boolean hasNext() {
            return entries.hasNext();
        }

        @Override
        public V next() {
            NbtDeserializer.this.stack.push(entries::next);

            var entry = valueEndec.decode(NbtDeserializer.this);

            NbtDeserializer.this.stack.pop();

            return entry;
        }
    }

    public class NbtMapDeserializer<V> implements MapDeserializer<V> {

        private final Iterator<Map.Entry<String, NbtElement>> entries;
        private final int maxSize;

        private final Endec<V> valueEndec;

        public NbtMapDeserializer(Map<String, NbtElement> map, Endec<V> valueEndec) {
            this.entries = map.entrySet().iterator();
            this.maxSize = map.size();

            this.valueEndec = valueEndec;
        }

        @Override
        public int size() {
            return maxSize;
        }

        @Override
        public boolean hasNext() {
            return entries.hasNext();
        }

        @Override
        public Map.Entry<String, V> next() {
            var entry = entries.next();

            NbtDeserializer.this.stack.push(entry::getValue);

            Map.Entry<String, V> newEntry = Map.entry(entry.getKey(), valueEndec.decode(NbtDeserializer.this));

            NbtDeserializer.this.stack.pop();

            return newEntry;
        }
    }

    public class NbtStructDeserializer implements StructDeserializer {

        private final Map<String, NbtElement> map;

        public NbtStructDeserializer(Map<String, NbtElement> map) {
            this.map = map;
        }

        @Override
        public <F> F field(String field, Endec<F> endec, @Nullable F defaultValue) {
            if(!map.containsKey(field)) return defaultValue;

            NbtDeserializer.this.stack.push(() -> map.get(field));

            F value = endec.decode(NbtDeserializer.this);

            NbtDeserializer.this.stack.pop();

            return value;
        }
    }
}
