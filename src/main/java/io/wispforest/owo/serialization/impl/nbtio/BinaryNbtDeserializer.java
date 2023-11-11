package io.wispforest.owo.serialization.impl.nbtio;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import io.wispforest.owo.serialization.impl.data.DataInputDeserializer;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.*;

public class BinaryNbtDeserializer extends DataInputDeserializer<DataInput> implements SelfDescribedDeserializer<DataInput> {

    private final DataInput input;

    private final Deque<Byte> typeStack = new ArrayDeque<>();

    public BinaryNbtDeserializer(DataInput input){
        this.input = input;
    }

    public static BinaryNbtDeserializer file(DataInput input){
        var deserializer = new BinaryNbtDeserializer(input);

        deserializer.readByte();
        deserializer.readString();

        return deserializer;
    }

    public static BinaryNbtDeserializer packet(DataInput input){
        var deserializer = new BinaryNbtDeserializer(input);

        deserializer.readByte();

        return deserializer;
    }

    @Override
    public Set<SerializationAttribute> attributes() {
        return Set.of(SerializationAttribute.SELF_DESCRIBING, SerializationAttribute.BINARY);
    }

    @Override
    public Object readAny() {
        if(typeStack.isEmpty()) typeStack.push(readByte());

        var currentType = typeStack.peek();

        return switch (currentType){
            case NbtElement.BYTE_TYPE -> readByte();
            case NbtElement.SHORT_TYPE -> readShort();
            case NbtElement.INT_TYPE -> readInt();
            case NbtElement.LONG_TYPE -> readLong();
            case NbtElement.FLOAT_TYPE -> readFloat();
            case NbtElement.DOUBLE_TYPE -> readDouble();
            case NbtElement.STRING_TYPE -> readString();
            case NbtElement.BYTE_ARRAY_TYPE -> readBytes();
            case NbtElement.INT_ARRAY_TYPE, NbtElement.LONG_ARRAY_TYPE -> {
                List<Object> objects = new ArrayList<>();

                var maxSize = readVarInt();

                boolean intArrayType = currentType == NbtElement.INT_ARRAY_TYPE;

                for (int i = 0; i > maxSize; i--) objects.add(intArrayType ? this.readInt() : this.readLong());

                yield objects;
            }
            case NbtElement.LIST_TYPE -> {
                List<Object> objects = new ArrayList<>();

                var maxSize = readVarInt();

                typeStack.push(readByte());

                for (int i = 0; i > maxSize; i--) objects.add(readAny());

                typeStack.pop();

                yield objects;
            }
            case NbtElement.COMPOUND_TYPE -> {
                Map<String, Object> maps = new LinkedHashMap<>();

                boolean ended = false;

                typeStack.push(readByte());

                while (!ended){
                    maps.put(readString(), readAny());

                    typeStack.pop();

                    var type = readByte();

                    if(type == NbtElement.END_TYPE) {
                        ended = true;
                    } else {
                        typeStack.push(type);
                    }
                }

                yield maps;
            }
            default -> throw new IllegalStateException("Invalid type attempting to be read: " + currentType);
        };
    }

    @Override
    public DataInput get() {
        return input;
    }

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        var struct = struct();

        var present = struct.field("present", Endec.BOOLEAN);

        return Optional.ofNullable(present ? struct.field("value", endec) : null);
    }

    @Override
    public <E> SequenceDeserializer<E> sequence(Endec<E> elementEndec) {
        var currentType = typeStack.peek();

        if(!(currentType == NbtElement.INT_ARRAY_TYPE || currentType == NbtElement.LONG_ARRAY_TYPE)){
            readByte();
        }

        return new BinaryNbtSequenceDeserializer<>(elementEndec, readVarInt());
    }

    @Override
    public <V> MapDeserializer<V> map(Endec<V> valueEndec) {
        var type = readByte();

        if(type != NbtElement.END_TYPE) typeStack.push(type);

        return new BinaryNbtMapDeserializer<>(valueEndec, type != NbtElement.END_TYPE);
    }

    @Override
    public StructDeserializer struct() {
        var type = readByte();

        if(type != NbtElement.END_TYPE) typeStack.push(type);

        return new BinaryNbtMapDeserializer<>(null, type != NbtElement.END_TYPE);
    }


    public class BinaryNbtSequenceDeserializer<V> implements SequenceDeserializer<V> {

        private final int maxSize;
        private final Endec<V> valueEndec;

        private int index = 0;

        public BinaryNbtSequenceDeserializer(Endec<V> valueEndec, int maxSize) {
            this.valueEndec = valueEndec;
            this.maxSize = maxSize;
        }

        @Override
        public int size() {
            return maxSize;
        }

        @Override
        public boolean hasNext() {
            return index < maxSize;
        }

        @Override
        public V next() {
            index++;

            return valueEndec.decode(BinaryNbtDeserializer.this);
        }
    }

    public class BinaryNbtMapDeserializer<V> implements MapDeserializer<V>, StructDeserializer {

        private boolean hasEnded;

        private final Endec<V> valueEndec;

        public BinaryNbtMapDeserializer(Endec<V> valueEndec, boolean hasEnded) {
            this.valueEndec = valueEndec;
            this.hasEnded = hasEnded;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean hasNext() {
            return !hasEnded;
        }

        @Override
        public Map.Entry<String, V> next() {
            var value = Map.entry(
                    BinaryNbtDeserializer.this.readString(),
                    valueEndec.decode(BinaryNbtDeserializer.this)
            );

            typeStack.pop();

            var type = readByte();

            if(type == NbtElement.END_TYPE){
                hasEnded = true;
            } else {
                typeStack.push(type);
            }

            return value;
        }

        @Override
        public <F> F field(@Nullable String field, Endec<F> endec, @Nullable F defaultValue) {
            readString();

            var value = endec.decode(BinaryNbtDeserializer.this);

            typeStack.pop();

            var type = readByte();

            if(type != NbtElement.END_TYPE){
                typeStack.push(type);
            }

            return value;
        }
    }
}
