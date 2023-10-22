package io.wispforest.owo.serialization.impl.json;

import com.google.gson.*;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Consumer;

public class JsonSerializer implements Serializer<JsonElement> {

    private final SerializationAttribute extraAttribute;

    protected Deque<Consumer<JsonElement>> stack = new ArrayDeque<>();

    protected JsonElement result = null;

    public JsonSerializer(boolean compressed) {
        stack.push(element -> result = element);

        extraAttribute = compressed ? SerializationAttribute.COMPRESSED : SerializationAttribute.HUMAN_READABLE;
    }

    public void consumeElement(JsonElement element) {
        stack.peek().accept(element);
    }

    //--

    public static JsonSerializer of(){
        return new JsonSerializer(false);
    }

    public static JsonSerializer compressed(){
        return new JsonSerializer(true);
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
    public <V> void writeOptional(Codeck<V> codeck, Optional<V> optional) {
        optional.ifPresentOrElse(v -> codeck.encode(this, v), () -> consumeElement(JsonNull.INSTANCE));
    }

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
    public void writeVarInt(int value) {
        writeInt(value);
    }

    @Override
    public void writeVarLong(long value) {
        writeLong(value);
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

            try {
                codec.encode(JsonSerializer.this, value);
            } finally {
                JsonSerializer.this.stack.pop();
            }

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

            try {
                valueCodec.encode(JsonSerializer.this, element);
            } finally {
                JsonSerializer.this.stack.pop();
            }

            if (encodedHolder.getValue() == null) throw new JsonSerializer.JsonEncodeException("No value was serialized");
            result.add(encodedHolder.getValue());
        }

        @Override
        public void end() {
            JsonSerializer.this.consumeElement(result);
        }
    }


}
