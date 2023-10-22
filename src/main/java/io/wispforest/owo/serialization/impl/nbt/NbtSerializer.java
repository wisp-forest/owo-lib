package io.wispforest.owo.serialization.impl.nbt;

import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import net.minecraft.nbt.*;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Consumer;

public class NbtSerializer implements SelfDescribedSerializer<NbtElement> {

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
        Set<SerializationAttribute> set = SelfDescribedSerializer.super.attributes();

        set.add(extraAttribute);

        return set;
    }

    //--

    @Override
    public void empty() {
        consumeElement(NbtEnd.INSTANCE);
    }

    @Override
    public void writeAny(Object object) {
        NbtElement element = null;

        if (object == null) {
            element = NbtEnd.INSTANCE;
        } else if(object instanceof String value){
            element = NbtString.of(value);
        } else if(object instanceof Boolean value) {
            element = NbtByte.of(value);
        } else if(object instanceof Byte value) {
            element = NbtByte.of(value);
        } else if(object instanceof Short value) {
            element = NbtShort.of(value);
        } else if(object instanceof Integer value) {
            element = NbtInt.of(value);
        } else if(object instanceof Long value) {
            element = NbtLong.of(value);
        } else if(object instanceof Float value) {
            element = NbtFloat.of(value);
        } else if(object instanceof Double value) {
            element = NbtDouble.of(value);
        } else if (object instanceof List objects ) {
            NbtList array = new NbtList();

            stack.push(array::add);

            try {
                objects.forEach(this::writeAny);
            } catch (UnsupportedOperationException e) {
                throw new FormatSerializeException("Unable to Serializer a List into Nbt Format due to differing entries types.", e);
            }

            stack.pop();

            if(array.getHeldType() == NbtElement.BYTE_TYPE){
                element = new NbtByteArray((List<Byte>) objects);
            } else if (array.getHeldType() == NbtElement.INT_TYPE){
                element = new NbtIntArray((List<Integer>) objects);
            } else if (array.getHeldType() == NbtElement.LONG_TYPE) {
                element = new NbtLongArray((List<Long>) objects);
            } else {
                element = array;
            }
        } else if (object instanceof Map map) {
            NbtCompound compound = new NbtCompound();

            map.forEach((key, value) -> {
                stack.push((element1) -> compound.put((String) key, element1));

                writeAny(value);

                stack.pop();
            });

            element = compound;
        } else {
            throw new IllegalStateException("Unknown Object type: " + element);
        }

        consumeElement(element);
    }

    //--

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
    public <E> SequenceSerializer<E> sequence(Codeck<E> elementCodec, int length) {
        return new NbtSequenceSerializer<>(elementCodec);
    }

    @Override
    public <V> MapSerializer<V> map(Codeck<V> valueCodec, int length) {
        return new NbtMapSerializer<V>().valueCodec(valueCodec);
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

        private Codeck<V> valueCodec = null;

        public NbtMapSerializer<V> valueCodec(Codeck<V> valueCodec) {
            this.valueCodec = valueCodec;

            return this;
        }

        @Override
        public void entry(String key, V value) {
            field(key, valueCodec, value);
        }

        @Override
        public <F> StructSerializer field(String name, Codeck<F> codec, F value) {
            MutableObject<NbtElement> encodedHolder = new MutableObject<>(null);

            NbtSerializer.this.stack.push(encodedHolder::setValue);

            try {
                codec.encode(NbtSerializer.this, value);
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

        private final Codeck<V> valueCodec;

        public NbtSequenceSerializer(Codeck<V> valueCodec) {
            this.valueCodec = valueCodec;
        }

        @Override
        public void element(V element) {
            MutableObject<NbtElement> encodedHolder = new MutableObject<>(null);

            NbtSerializer.this.stack.push(encodedHolder::setValue);

            try {
                valueCodec.encode(NbtSerializer.this, element);
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
