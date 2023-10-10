package io.wispforest.owo.serialization.impl.nbt;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.json.JsonDeserializer;
import io.wispforest.owo.serialization.impl.json.JsonSerializer;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class NbtDeserializer implements SelfDescribedDeserializer<NbtElement> {

    protected final Deque<Supplier<NbtElement>> stack = new ArrayDeque<>();

    private boolean unsafe = true;

    public NbtDeserializer(NbtElement element) {
        stack.push(() -> element);
    }

    public NbtDeserializer unsafe(boolean value) {
        this.unsafe = value;

        return this;
    }

    private NbtElement topElement() {
        return stack.peek().get();
    }

    //--

    @Override
    public NbtElement getEmpty() {
        return NbtEnd.INSTANCE;
    }

    @Override
    public Object any() {
        return null;
    }

    //--

    @Override
    public <V> Optional<V> readOptional(Codeck<V> codeck) {
        var element = topElement();

        if(element == getEmpty()) return Optional.empty();

        return Optional.of(codeck.decode(this));
    }

    @Override
    public boolean readBoolean() {
        if (topElement() instanceof NbtByte nbtNumber) return nbtNumber.byteValue() != 0;

        if (unsafe) return false; // Default value: 0 != 0;

        throw new RuntimeException("[NbtFormat] input was not NbtByte for a Boolean get call");
    }

    @Override
    public byte readByte() {
        if (topElement() instanceof NbtByte nbtNumber) return nbtNumber.byteValue();

        if (unsafe) return 0;

        throw new RuntimeException("[NbtFormat] input was not NbtByte for a Byte get call");
    }

    @Override
    public short readShort() {
        if (topElement() instanceof NbtShort nbtNumber) return nbtNumber.shortValue();

        if (unsafe) return 0;

        throw new RuntimeException("[NbtFormat] input was not NbtShort");
    }

    @Override
    public int readInt() {
        if (topElement() instanceof NbtInt nbtNumber) return nbtNumber.intValue();

        if (unsafe) return 0;

        throw new RuntimeException("[NbtFormat] input was not NbtInt");
    }

    @Override
    public long readLong() {
        if (topElement() instanceof NbtLong nbtNumber) return nbtNumber.longValue();

        if (unsafe) return 0L;

        throw new RuntimeException("[NbtFormat] input was not NbtLong");
    }

    @Override
    public float readFloat() {
        if (topElement() instanceof NbtFloat nbtNumber) return nbtNumber.floatValue();

        if (unsafe) return 0F;

        throw new RuntimeException("[NbtFormat] input was not NbtFloat");
    }

    @Override
    public double readDouble() {
        if (topElement() instanceof NbtDouble nbtNumber) return nbtNumber.doubleValue();

        if (unsafe) return 0D;

        throw new RuntimeException("[NbtFormat] input was not NbtDouble");
    }

    @Override
    public String readString() {
        if (topElement() instanceof NbtString nbtString) return nbtString.asString();

        if (unsafe) return "";

        throw new RuntimeException("[NbtFormat] input was not NbtDouble");
    }

    @Override
    public byte[] readBytes() {
        if(topElement() instanceof NbtByteArray nbtBytesArray) return nbtBytesArray.getByteArray();

        if (unsafe) return new byte[0];

        throw new RuntimeException("[NbtFormat] input was not NbtByteArray");
    }

    @Override
    public <E> SequenceDeserializer<E> sequence(Codeck<E> elementCodec) {
        return new NbtSequenceDeserializer<>(((AbstractNbtList<NbtElement>) topElement()), elementCodec);
    }

    @Override
    public <V> MapDeserializer<V> map(Codeck<V> valueCodec) {
        return new NbtMapDeserializer<>(((NbtCompound) topElement()).toMap(), valueCodec);
    }

    @Override
    public StructDeserializer struct() {
        return new NbtStructDeserializer(((NbtCompound) topElement()).toMap());
    }

    public class NbtSequenceDeserializer<V> implements SequenceDeserializer<V> {

        private final Iterator<NbtElement> entries;
        private final Codeck<V> valueCodec;

        public NbtSequenceDeserializer(AbstractNbtList<NbtElement> entries, Codeck<V> valueCodec) {
            this.entries = entries.iterator();
            this.valueCodec = valueCodec;
        }

        @Override
        public boolean hasNext() {
            return entries.hasNext();
        }

        @Override
        public V next() {
            NbtDeserializer.this.stack.push(entries::next);

            var entry = valueCodec.decode(NbtDeserializer.this);

            NbtDeserializer.this.stack.pop();

            return entry;
        }
    }

    public class NbtMapDeserializer<V> implements MapDeserializer<V> {

        private final Iterator<Map.Entry<String, NbtElement>> entries;
        private final Codeck<V> valueCodec;

        public NbtMapDeserializer(Map<String, NbtElement> map, Codeck<V> valueCodec) {
            this.entries = map.entrySet().iterator();
            this.valueCodec = valueCodec;
        }

        @Override
        public boolean hasNext() {
            return entries.hasNext();
        }

        @Override
        public Map.Entry<String, V> next() {
            var entry = entries.next();

            NbtDeserializer.this.stack.push(entry::getValue);

            var newEntry = Map.entry(entry.getKey(), valueCodec.decode(NbtDeserializer.this));

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
        public <F> F field(String field, Codeck<F> codeck, @Nullable F defaultValue) {
            if(!map.containsKey(field)) return defaultValue;

            NbtDeserializer.this.stack.push(() -> map.get(field));

            var value = codeck.decode(NbtDeserializer.this);

            NbtDeserializer.this.stack.pop();

            return value;
        }
    }
}
