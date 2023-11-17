package io.wispforest.owo.serialization.impl.json;

import com.google.gson.*;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class JsonSerializer extends HierarchicalSerializer<JsonElement> {

    private static final Set<SerializationAttribute> ATTRIBUTES = EnumSet.allOf(SerializationAttribute.class);
    protected JsonElement prefix;

    private JsonSerializer(JsonElement prefix) {
        super(null);
        this.prefix = prefix;
    }

    //--

    public static JsonSerializer of() {
        return new JsonSerializer(null);
    }

    public static JsonSerializer of(JsonElement prefix) {
        return new JsonSerializer(prefix);
    }

    //--

    @Override
    public Set<SerializationAttribute> attributes() {
        return ATTRIBUTES;
    }

    //--

    @Override
    public <V> void writeOptional(Endec<V> endec, Optional<V> optional) {
        optional.ifPresentOrElse(
                value -> endec.encode(this, value),
                () -> this.consume(JsonNull.INSTANCE)
        );
    }

    @Override
    public void writeBoolean(boolean value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeByte(byte value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeShort(short value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeInt(int value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeLong(long value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeFloat(float value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeDouble(double value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeString(String value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeBytes(byte[] bytes) {
        JsonArray array = new JsonArray();

        for (byte aByte : bytes) array.add(aByte);

        this.consume(array);
        //consumeElement(new JsonPrimitive(Base64.encodeBase64String(bytes)));
    }

    @Override
    public void writeVarInt(int value) {
        this.writeInt(value);
    }

    @Override
    public void writeVarLong(long value) {
        this.writeLong(value);
    }

    @Override
    public <E> SequenceSerializer<E> sequence(Endec<E> elementEndec, int size) {
        return new JsonSequenceSerializer<>(elementEndec, size);
    }

    @Override
    public <V> MapSerializer<V> map(Endec<V> valueEndec, int size) {
        return new JsonMapSerializer<>(valueEndec);
    }

    @Override
    public StructSerializer struct() {
        return new JsonMapSerializer<>(null);
    }

    @Override
    public JsonElement result() {
        return this.result;
    }

    public class JsonMapSerializer<V> implements MapSerializer<V>, StructSerializer {

        private final Endec<V> valueEndec;
        private final JsonObject result;

        public JsonMapSerializer(Endec<V> valueEndec) {
            this.valueEndec = valueEndec;

            if (JsonSerializer.this.prefix != null) {
                if (JsonSerializer.this.prefix instanceof JsonObject prefixObject) {
                    this.result = prefixObject;
                } else {
                    throw new IllegalStateException("Incompatible prefix of type " + JsonSerializer.this.prefix.getClass().getSimpleName() + " used for JSON map/struct");
                }
            } else {
                this.result = new JsonObject();
            }
        }

        @Override
        public void entry(String key, V value) {
            JsonSerializer.this.frame(encoded -> {
                this.valueEndec.encode(JsonSerializer.this, value);
                this.result.add(key, encoded.require("map value"));
            });
        }

        @Override
        public <F> StructSerializer field(String name, Endec<F> endec, F value) {
            JsonSerializer.this.frame(encoded -> {
                endec.encode(JsonSerializer.this, value);
                this.result.add(name, encoded.require("struct field"));
            });

            return this;
        }

        @Override
        public void end() {
            JsonSerializer.this.consume(result);
        }
    }

    public class JsonSequenceSerializer<V> implements SequenceSerializer<V> {

        private final Endec<V> valueEndec;
        private final JsonArray result;

        public JsonSequenceSerializer(Endec<V> valueEndec, int size) {
            this.valueEndec = valueEndec;

            if (JsonSerializer.this.prefix != null) {
                if (JsonSerializer.this.prefix instanceof JsonArray prefixArray) {
                    this.result = prefixArray;
                } else {
                    throw new IllegalStateException("Incompatible prefix of type " + JsonSerializer.this.prefix.getClass().getSimpleName() + " used for JSON sequence");
                }
            } else {
                this.result = new JsonArray(size);
            }
        }

        @Override
        public void element(V element) {
            JsonSerializer.this.frame(encoded -> {
                this.valueEndec.encode(JsonSerializer.this, element);
                this.result.add(encoded.require("sequence element"));
            });
        }

        @Override
        public void end() {
            JsonSerializer.this.consume(result);
        }
    }


}
