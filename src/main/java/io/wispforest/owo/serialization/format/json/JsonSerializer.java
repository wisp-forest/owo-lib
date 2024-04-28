package io.wispforest.owo.serialization.format.json;

import com.google.gson.*;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.util.RecursiveSerializer;
import io.wispforest.owo.ui.hud.Hud;

import java.util.Optional;

public class JsonSerializer extends RecursiveSerializer<JsonElement> implements SelfDescribedSerializer<JsonElement> {

    private JsonElement prefix;

    protected JsonSerializer(JsonElement prefix) {
        super(null);
        this.prefix = prefix;
    }

    public static JsonSerializer of(JsonElement prefix) {
        return new JsonSerializer(prefix);
    }

    public static JsonSerializer of() {
        return of(null);
    }

    @Override
    public SerializationContext setupContext(SerializationContext ctx) {
        return super.setupContext(ctx).withAttributes(SerializationAttributes.HUMAN_READABLE);
    }

    // ---

    @Override
    public void writeByte(SerializationContext ctx, byte value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeShort(SerializationContext ctx, short value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeInt(SerializationContext ctx, int value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeLong(SerializationContext ctx, long value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeFloat(SerializationContext ctx, float value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeDouble(SerializationContext ctx, double value) {
        this.consume(new JsonPrimitive(value));
    }

    // ---

    @Override
    public void writeVarInt(SerializationContext ctx, int value) {
        this.writeInt(ctx, value);
    }

    @Override
    public void writeVarLong(SerializationContext ctx, long value) {
        this.writeLong(ctx, value);
    }

    // ---

    @Override
    public void writeBoolean(SerializationContext ctx, boolean value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeString(SerializationContext ctx, String value) {
        this.consume(new JsonPrimitive(value));
    }

    @Override
    public void writeBytes(SerializationContext ctx, byte[] bytes) {
        var result = new JsonArray(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            result.add(bytes[i]);
        }

        this.consume(result);
    }

    @Override
    public <V> void writeOptional(SerializationContext ctx, Endec<V> endec, Optional<V> optional) {
        if (this.isWritingStructField()) {
            optional.ifPresent(value -> endec.encode(ctx, this, value));
        } else {
            optional.ifPresentOrElse(
                    value -> endec.encode(ctx, this, value),
                    () -> this.consume(JsonNull.INSTANCE)
            );
        }
    }

    // ---

    @Override
    public <E> Serializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec, int size) {
        return new Sequence<>(ctx, elementEndec, size);
    }

    @Override
    public <V> Serializer.Map<V> map(SerializationContext ctx, Endec<V> valueEndec, int size) {
        return new Map<>(ctx, valueEndec);
    }

    @Override
    public Struct struct() {
        return new Map<>(null, null);
    }

    // ---

    private class Map<V> implements Serializer.Map<V>, Struct {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final JsonObject result;

        private Map(SerializationContext ctx, Endec<V> valueEndec) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            if (JsonSerializer.this.prefix != null) {
                if (JsonSerializer.this.prefix instanceof JsonObject prefixObject) {
                    this.result = prefixObject;
                    JsonSerializer.this.prefix = null;
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
                this.valueEndec.encode(this.ctx, JsonSerializer.this, value);
                this.result.add(key, encoded.require("map value"));
            }, false);
        }

        @Override
        public <F> Struct field(String name, SerializationContext ctx, Endec<F> endec, F value) {
            JsonSerializer.this.frame(encoded -> {
                endec.encode(ctx, JsonSerializer.this, value);
                this.result.add(name, encoded.require("struct field"));
            }, true);

            return this;
        }

        @Override
        public void end() {
            JsonSerializer.this.consume(result);
        }
    }

    private class Sequence<V> implements Serializer.Sequence<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final JsonArray result;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec, int size) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            if (JsonSerializer.this.prefix != null) {
                if (JsonSerializer.this.prefix instanceof JsonArray prefixArray) {
                    this.result = prefixArray;
                    JsonSerializer.this.prefix = null;
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
                this.valueEndec.encode(this.ctx, JsonSerializer.this, element);
                this.result.add(encoded.require("sequence element"));
            }, false);
        }

        @Override
        public void end() {
            JsonSerializer.this.consume(result);
        }
    }
}
