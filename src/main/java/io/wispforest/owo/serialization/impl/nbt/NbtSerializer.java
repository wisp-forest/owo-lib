package io.wispforest.owo.serialization.impl.nbt;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import net.minecraft.nbt.*;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Consumer;

public class NbtSerializer implements Serializer<NbtElement> {

    private final SerializationAttribute extraAttribute;

    private final Deque<Consumer<NbtElement>> stack = new ArrayDeque<>();

    private NbtElement result = null;

    public NbtSerializer(SerializationAttribute attribute) {
        stack.push(element -> result = element);

        extraAttribute = attribute;
    }

    private void consumeElement(NbtElement element) {
        stack.peek().accept(element);
    }

    //--

    public static NbtSerializer of(){
        return new NbtSerializer(SerializationAttribute.HUMAN_READABLE);
    }

    public static NbtSerializer compressed(){
        return new NbtSerializer(SerializationAttribute.COMPRESSED);
    }

    public static NbtSerializer binary(){
        return new NbtSerializer(SerializationAttribute.BINARY);
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
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        optional.ifPresentOrElse(v -> endec.encode(this, v), () -> consumeElement(NbtEnd.INSTANCE));
    }

    @Override
    public void writeBoolean(boolean value) {
        consumeElement(NbtByte.of(value));
    }

    @Override
    public void writeByte(byte value) {
        consumeElement(NbtByte.of(value));
    }

    @Override
    public void writeShort(short value) {
        consumeElement(NbtShort.of(value));
    }

    @Override
    public void writeInt(int value) {
        consumeElement(NbtInt.of(value));
    }

    @Override
    public void writeLong(long value) {
        consumeElement(NbtLong.of(value));
    }

    @Override
    public void writeFloat(float value) {
        consumeElement(NbtFloat.of(value));
    }

    @Override
    public void writeDouble(double value) {
        consumeElement(NbtDouble.of(value));
    }

    @Override
    public void writeString(String value) {
        consumeElement(NbtString.of(value));
    }

    @Override
    public void writeBytes(byte[] bytes) {
        consumeElement(new NbtByteArray(bytes));
    }

    @Override
    public void writeVarInt(int value) {
        var abstractNbtNumber = switch (VarInts.getSizeInBytes(value)){
            case 0, 1 -> NbtByte.of((byte) value);
            case 2 -> NbtShort.of((short) value);
            default -> NbtInt.of(value);
        };

        consumeElement(abstractNbtNumber);
    }

    @Override
    public void writeVarLong(long value) {
        var abstractNbtNumber = switch (VarLongs.getSizeInBytes(value)){
            case 0, 1 -> NbtByte.of((byte) value);
            case 2 -> NbtShort.of((short) value);
            case 3, 4 -> NbtInt.of((int) value);
            default -> NbtLong.of(value);
        };

        consumeElement(abstractNbtNumber);
    }

    @Override
    public <E> SequenceSerializer<E> sequence(Endec<E> elementEndec, int length) {
        return new NbtSequenceSerializer<>(elementEndec);
    }

    @Override
    public <V> MapSerializer<V> map(Endec<V> valueEndec, int length) {
        return new NbtMapSerializer<V>().valueEndec(valueEndec);
    }

    @Override
    public StructSerializer struct() {
        return new NbtMapSerializer<>();
    }

    @Override
    public NbtElement result() {
        return result;
    }

    public static class NbtEncodeException extends RuntimeException {
        public NbtEncodeException(String message) {
            super(message);
        }
    }

    public class NbtMapSerializer<V> implements MapSerializer<V>, StructSerializer {

        private final NbtCompound result = new NbtCompound();

        private Endec<V> valueEndec = null;

        public NbtMapSerializer<V> valueEndec(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;

            return this;
        }

        @Override
        public void entry(String key, V value) {
            field(key, valueEndec, value);
        }

        @Override
        public <F> StructSerializer field(String name, Endec<F> endec, F value) {
            MutableObject<NbtElement> encodedHolder = new MutableObject<>(null);

            NbtSerializer.this.stack.push(encodedHolder::setValue);

            try {
                endec.encode(NbtSerializer.this, value);
            } finally {
                NbtSerializer.this.stack.pop();
            }

            if (encodedHolder.getValue() == null) throw new NbtSerializer.NbtEncodeException("No field was serialized");
            result.put(name, encodedHolder.getValue());

            return this;
        }

        @Override
        public void end() {
            NbtSerializer.this.consumeElement(result);
        }
    }

    public class NbtSequenceSerializer<V> implements SequenceSerializer<V> {

        private final NbtList result = new NbtList();

        private final Endec<V> valueEndec;

        public NbtSequenceSerializer(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;
        }

        @Override
        public void element(V element) {
            MutableObject<NbtElement> encodedHolder = new MutableObject<>(null);

            NbtSerializer.this.stack.push(encodedHolder::setValue);

            try {
                valueEndec.encode(NbtSerializer.this, element);
            } finally {
                NbtSerializer.this.stack.pop();
            }

            if (encodedHolder.getValue() == null) throw new NbtSerializer.NbtEncodeException("No value was serialized");
            result.add(encodedHolder.getValue());
        }

        @Override
        public void end() {
            NbtSerializer.this.consumeElement(result);
        }
    }

}
