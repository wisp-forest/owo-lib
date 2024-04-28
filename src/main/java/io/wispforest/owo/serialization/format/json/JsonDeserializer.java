package io.wispforest.owo.serialization.format.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.util.RecursiveDeserializer;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

public class JsonDeserializer extends RecursiveDeserializer<JsonElement> implements SelfDescribedDeserializer<JsonElement> {

    protected JsonDeserializer(JsonElement serialized) {
        super(serialized);
    }

    public static JsonDeserializer of(JsonElement serialized) {
        return new JsonDeserializer(serialized);
    }

    @Override
    public SerializationContext setupContext(SerializationContext ctx) {
        return super.setupContext(ctx).withAttributes(SerializationAttributes.HUMAN_READABLE);
    }

    // ---

    @Override
    public byte readByte(SerializationContext ctx) {
        return this.getValue().getAsByte();
    }

    @Override
    public short readShort(SerializationContext ctx) {
        return this.getValue().getAsShort();
    }

    @Override
    public int readInt(SerializationContext ctx) {
        return this.getValue().getAsInt();
    }

    @Override
    public long readLong(SerializationContext ctx) {
        return this.getValue().getAsLong();
    }

    @Override
    public float readFloat(SerializationContext ctx) {
        return this.getValue().getAsFloat();
    }

    @Override
    public double readDouble(SerializationContext ctx) {
        return this.getValue().getAsDouble();
    }

    // ---

    @Override
    public int readVarInt(SerializationContext ctx) {
        return this.readInt(ctx);
    }

    @Override
    public long readVarLong(SerializationContext ctx) {
        return this.readLong(ctx);
    }

    // ---

    @Override
    public boolean readBoolean(SerializationContext ctx) {
        return this.getValue().getAsBoolean();
    }

    @Override
    public String readString(SerializationContext ctx) {
        return this.getValue().getAsString();
    }

    @Override
    public byte[] readBytes(SerializationContext ctx) {
        var array = this.getValue().getAsJsonArray().asList();

        var result = new byte[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i).getAsByte();
        }

        return result;
    }

    @Override
    public <V> Optional<V> readOptional(SerializationContext ctx, Endec<V> endec) {
        var value = this.getValue();
        return !value.isJsonNull()
                ? Optional.of(endec.decode(ctx, this))
                : Optional.empty();
    }

    // ---

    @Override
    public <E> Deserializer.Sequence<E> sequence(SerializationContext ctx, Endec<E> elementEndec) {
        return new Sequence<>(ctx, elementEndec, (JsonArray) this.getValue());
    }

    @Override
    public <V> Deserializer.Map<V> map(SerializationContext ctx, Endec<V> valueEndec) {
        return new Map<>(ctx, valueEndec, ((JsonObject) this.getValue()));
    }

    @Override
    public Deserializer.Struct struct() {
        return new Struct((JsonObject) this.getValue());
    }

    // ---

    @Override
    public <S> void readAny(SerializationContext ctx, Serializer<S> visitor) {
        this.decodeValue(ctx, visitor, this.getValue());
    }

    private <S> void decodeValue(SerializationContext ctx, Serializer<S> visitor, JsonElement element) {
        if (element.isJsonNull()) {
            visitor.writeOptional(ctx, JsonEndec.INSTANCE, Optional.empty());
        } else if (element instanceof JsonPrimitive primitive) {
            if (primitive.isString()) {
                visitor.writeString(ctx, primitive.getAsString());
            } else if (primitive.isBoolean()) {
                visitor.writeBoolean(ctx, primitive.getAsBoolean());
            } else {
                var value = primitive.getAsBigDecimal();

                try {
                    var asLong = value.longValueExact();

                    if ((byte) asLong == asLong) {
                        visitor.writeByte(ctx, element.getAsByte());
                    } else if ((short) asLong == asLong) {
                        visitor.writeShort(ctx, element.getAsShort());
                    } else if ((int) asLong == asLong) {
                        visitor.writeInt(ctx, element.getAsInt());
                    } else {
                        visitor.writeLong(ctx, asLong);
                    }
                } catch (ArithmeticException bruh /* quite cringe java moment, why use an exception for this */) {
                    var asDouble = value.doubleValue();

                    if ((float) asDouble == asDouble) {
                        visitor.writeFloat(ctx, element.getAsFloat());
                    } else {
                        visitor.writeDouble(ctx, asDouble);
                    }
                }
            }
        } else if (element instanceof JsonArray array) {
            try (var sequence = visitor.sequence(ctx, Endec.<JsonElement>of(this::decodeValue, (ctx1, deserializer) -> null), array.size())) {
                array.forEach(sequence::element);
            }
        } else if (element instanceof JsonObject object) {
            try (var map = visitor.map(ctx, Endec.<JsonElement>of(this::decodeValue, (ctx1, deserializer) -> null), object.size())) {
                object.asMap().forEach(map::entry);
            }
        } else {
            throw new IllegalArgumentException("Non-standard, unrecognized JsonElement implementation cannot be decoded");
        }
    }

    // ---

    private class Sequence<V> implements Deserializer.Sequence<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final Iterator<JsonElement> elements;
        private final int size;

        private Sequence(SerializationContext ctx, Endec<V> valueEndec, JsonArray elements) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            this.elements = elements.iterator();
            this.size = elements.size();
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.elements.hasNext();
        }

        @Override
        public V next() {
            return JsonDeserializer.this.frame(
                    this.elements::next,
                    () -> this.valueEndec.decode(this.ctx, JsonDeserializer.this),
                    false
            );
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final SerializationContext ctx;
        private final Endec<V> valueEndec;
        private final Iterator<java.util.Map.Entry<String, JsonElement>> entries;
        private final int size;

        private Map(SerializationContext ctx, Endec<V> valueEndec, JsonObject entries) {
            this.ctx = ctx;
            this.valueEndec = valueEndec;

            this.entries = entries.entrySet().iterator();
            this.size = entries.size();
        }

        @Override
        public int estimatedSize() {
            return this.size;
        }

        @Override
        public boolean hasNext() {
            return this.entries.hasNext();
        }

        @Override
        public java.util.Map.Entry<String, V> next() {
            var entry = entries.next();
            return JsonDeserializer.this.frame(
                    entry::getValue,
                    () -> java.util.Map.entry(entry.getKey(), this.valueEndec.decode(this.ctx, JsonDeserializer.this)),
                    false
            );
        }
    }

    private class Struct implements Deserializer.Struct {

        private final JsonObject object;

        private Struct(JsonObject object) {
            this.object = object;
        }

        @Override
        public <F> @Nullable F field(String name, SerializationContext ctx, Endec<F> endec) {
            if (!this.object.has(name)) {
                throw new IllegalStateException("Field '" + name + "' was missing from serialized data, but no default value was provided");
            }
            return JsonDeserializer.this.frame(
                    () -> this.object.get(name),
                    () -> endec.decode(ctx, JsonDeserializer.this),
                    true
            );
        }

        @Override
        public <F> @Nullable F field(String name, SerializationContext ctx, Endec<F> endec, @Nullable F defaultValue) {
            if (!this.object.has(name)) return defaultValue;
            return JsonDeserializer.this.frame(
                    () -> this.object.get(name),
                    () -> endec.decode(ctx, JsonDeserializer.this),
                    true
            );
        }
    }
}
