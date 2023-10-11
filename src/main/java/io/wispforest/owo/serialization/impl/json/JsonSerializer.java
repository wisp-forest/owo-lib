package io.wispforest.owo.serialization.impl.json;

import com.google.gson.*;
import io.wispforest.owo.serialization.MapSerializer;
import io.wispforest.owo.serialization.SelfDescribedSerializer;
import io.wispforest.owo.serialization.SequenceSerializer;
import io.wispforest.owo.serialization.StructSerializer;
import io.wispforest.owo.serialization.Codeck;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Consumer;

public class JsonSerializer implements SelfDescribedSerializer<JsonElement> {

    protected Deque<Consumer<JsonElement>> stack = new ArrayDeque<>();

    protected JsonElement result = null;

    public JsonSerializer() {
        stack.push(element -> result = element);
    }

    public void consumeElement(JsonElement element) {
        stack.peek().accept(element);
    }

    @Override
    public void empty() {
        consumeElement(JsonNull.INSTANCE); //Add ability to change such to write or no write ability
    }

    @Override
    public void writeAny(Object object) {
        JsonElement element = null;

        if (object == null) {
            element = JsonNull.INSTANCE;
        } else if(object instanceof String value){
            element = new JsonPrimitive(value);
        } else if(object instanceof Boolean value) {
            element = new JsonPrimitive(value);
        } else if(object instanceof Byte value) {
            element = new JsonPrimitive(value);
        } else if(object instanceof Short value) {
            element = new JsonPrimitive(value);
        } else if(object instanceof Integer value) {
            element = new JsonPrimitive(value);
        } else if(object instanceof Long value) {
            element = new JsonPrimitive(value);
        } else if(object instanceof Float value) {
            element = new JsonPrimitive(value);
        } else if(object instanceof Double value) {
            element = new JsonPrimitive(value);
        } else if (object instanceof List objects) {
            JsonArray array = new JsonArray();

            stack.push(array::add);
            objects.forEach(this::writeAny);
            stack.pop();

            element = array;
        } else if (object instanceof Map map) {
            JsonObject jsonObject = new JsonObject();

            map.forEach((key, value) -> {
                stack.push((element1) -> jsonObject.add((String) key, element1));

                writeAny(value);

                stack.pop();
            });

            element = jsonObject;
        } else {
            throw new IllegalStateException("Unknown Object type: " + object);
        }

        consumeElement(element);
    }

    //--

    @Override
    public void writeBoolean(boolean value) {
        consumeElement(new JsonPrimitive(value));
    }

    @Override
    public void writeByte(byte value) {
        consumeElement(new JsonPrimitive(value));
    }

    @Override
    public void writeShort(short value) {
        consumeElement(new JsonPrimitive(value));
    }

    @Override
    public void writeInt(int value) {
        consumeElement(new JsonPrimitive(value));
    }

    @Override
    public void writeLong(long value) {
        consumeElement(new JsonPrimitive(value));
    }

    @Override
    public void writeFloat(float value) {
        consumeElement(new JsonPrimitive(value));
    }

    @Override
    public void writeDouble(double value) {
        consumeElement(new JsonPrimitive(value));
    }

    @Override
    public void writeString(String value) {
        consumeElement(new JsonPrimitive(value));
    }

    @Override
    public void writeBytes(byte[] bytes) {
        JsonArray array = new JsonArray();

        for (byte aByte : bytes) array.add(aByte);

        consumeElement(array);
        //consumeElement(new JsonPrimitive(Base64.encodeBase64String(bytes)));
    }

    @Override
    public <E> SequenceSerializer<E> sequence(Codeck<E> elementCodec, int length) {
        return new JsonSequenceSerializer<>(elementCodec);
    }

    @Override
    public <V> MapSerializer<V> map(Codeck<V> valueCodec, int length) {
        return new JsonMapSerializer<V>().valueCodec(valueCodec);
    }

    @Override
    public StructSerializer struct() {
        return new JsonMapSerializer<>();
    }

    @Override
    public JsonElement result() {
        return result;
    }

    public static class JsonEncodeException extends RuntimeException {
        public JsonEncodeException(String message) {
            super(message);
        }
    }

    public class JsonMapSerializer<V> implements MapSerializer<V>, StructSerializer {

        private final JsonObject result = new JsonObject();

        private Codeck<V> valueCodec = null;

        public JsonMapSerializer<V> valueCodec(Codeck<V> valueCodec) {
            this.valueCodec = valueCodec;

            return this;
        }

        @Override
        public void entry(String key, V value) {
            field(key, valueCodec, value);
        }

        @Override
        public <F> StructSerializer field(String name, Codeck<F> codec, F value) {
            MutableObject<JsonElement> encodedHolder = new MutableObject<>(null);

            JsonSerializer.this.stack.push(encodedHolder::setValue);
            codec.encode(JsonSerializer.this, value);
            JsonSerializer.this.stack.pop();

            if (encodedHolder.getValue() == null) throw new JsonSerializer.JsonEncodeException("No field was serialized");
            result.add(name, encodedHolder.getValue());

            return this;
        }

        @Override
        public void end() {
            JsonSerializer.this.consumeElement(result);
        }
    }

    public class JsonSequenceSerializer<V> implements SequenceSerializer<V> {

        private final Codeck<V> valueCodec;

        private final JsonArray result = new JsonArray();

        public JsonSequenceSerializer(Codeck<V> valueCodec) {
            this.valueCodec = valueCodec;
        }

        @Override
        public void element(V element) {
            MutableObject<JsonElement> encodedHolder = new MutableObject<>(null);

            JsonSerializer.this.stack.push(encodedHolder::setValue);
            valueCodec.encode(JsonSerializer.this, element);
            JsonSerializer.this.stack.pop();

            if (encodedHolder.getValue() == null) throw new JsonSerializer.JsonEncodeException("No value was serialized");
            result.add(encodedHolder.getValue());
        }

        @Override
        public void end() {
            JsonSerializer.this.consumeElement(result);
        }
    }


}
