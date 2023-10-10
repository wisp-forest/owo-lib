package io.wispforest.owo.serialization.impl.nbt;

import io.wispforest.owo.serialization.*;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NbtSerializer implements SelfDescribedSerializer<NbtElement> {

    protected Deque<Consumer<NbtElement>> stack = new ArrayDeque<>();

    protected NbtElement result = null;

    public NbtSerializer() {
        stack.push(element -> result = element);
    }

    public void consumeElement(NbtElement element) {
        stack.peek().accept(element);
    }

    @Override
    public void empty() {
        consumeElement(NbtEnd.INSTANCE);
    }

    //--

    @Override
    public void readAny(Object object) {
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
                objects.forEach(this::readAny);
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
        } else if (element instanceof Map map) {
            NbtCompound compound = new NbtCompound();

            map.forEach((key, value) -> {
                stack.push((element1) -> compound.put((String) key, element1));

                readAny(value);

                stack.pop();
            });

            element = compound;
        } else {
            throw new IllegalStateException("Unknown Object type: " + element);
        }

        consumeElement(element);
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
            codec.encode(NbtSerializer.this, value);
            NbtSerializer.this.stack.pop();

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
            valueCodec.encode(NbtSerializer.this, element);
            NbtSerializer.this.stack.pop();

            if (encodedHolder.getValue() == null) throw new NbtSerializer.NbtEncodeException("No value was serialized");
            result.add(encodedHolder.getValue());
        }

        @Override
        public void end() {
            NbtSerializer.this.consumeElement(result);
        }
    }

}
