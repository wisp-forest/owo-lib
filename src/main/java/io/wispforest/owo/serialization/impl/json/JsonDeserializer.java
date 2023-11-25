package io.wispforest.owo.serialization.impl.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class JsonDeserializer extends HierarchicalDeserializer<JsonElement> implements SelfDescribedDeserializer<JsonElement> {

    private static final Set<SerializationAttribute> ATTRIBUTES = EnumSet.of(
            SerializationAttribute.SELF_DESCRIBING,
            SerializationAttribute.HUMAN_READABLE
    );

    public JsonDeserializer(JsonElement serialized) {
        super(serialized);
    }

    // ---

    public static JsonDeserializer of(JsonElement serialized) {
        return new JsonDeserializer(serialized);
    }

    @Override
    public Set<SerializationAttribute> attributes() {
        return ATTRIBUTES;
    }

    // ---

    @Override
    public byte readByte() {
        return this.getValue().getAsByte();
    }

    @Override
    public short readShort() {
        return this.getValue().getAsShort();
    }

    @Override
    public int readInt() {
        return this.getValue().getAsInt();
    }

    @Override
    public long readLong() {
        return this.getValue().getAsLong();
    }

    @Override
    public float readFloat() {
        return this.getValue().getAsFloat();
    }

    @Override
    public double readDouble() {
        return this.getValue().getAsDouble();
    }

    // ---

    @Override
    public int readVarInt() {
        return this.readInt();
    }

    @Override
    public long readVarLong() {
        return this.readLong();
    }

    // ---

    @Override
    public boolean readBoolean() {
        return this.getValue().getAsBoolean();
    }

    @Override
    public String readString() {
        return this.getValue().getAsString();
    }

    @Override
    public byte[] readBytes() {
        var array = this.getValue().getAsJsonArray().asList();

        var result = new byte[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i).getAsByte();
        }

        return result;
    }

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        var value = this.getValue();
        return !value.isJsonNull()
                ? Optional.of(endec.decode(this))
                : Optional.empty();
    }

    // ---

    @Override
    public <E> Deserializer.Sequence<E> sequence(Endec<E> elementEndec) {
        return new Sequence<>(elementEndec, (JsonArray) this.getValue());
    }

    @Override
    public <V> Deserializer.Map<V> map(Endec<V> valueEndec) {
        return new Map<>(valueEndec, ((JsonObject) this.getValue()));
    }

    @Override
    public Deserializer.Struct struct() {
        return new Struct((JsonObject) this.getValue());
    }

    // ---

    @Override
    public <S> void readAny(Serializer<S> visitor) {
        this.decodeValue(visitor, this.getValue());
    }

    private <S> void decodeValue(Serializer<S> visitor, JsonElement element) {
        if (element.isJsonNull()) {
            visitor.writeOptional(JsonEndec.INSTANCE, Optional.empty());
        } else if (element instanceof JsonPrimitive primitive) {
            if (primitive.isString()) {
                visitor.writeString(primitive.getAsString());
            } else if (primitive.isBoolean()) {
                visitor.writeBoolean(primitive.getAsBoolean());
            } else {
                var value = primitive.getAsBigDecimal();

                try {
                    var asLong = value.longValueExact();

                    if ((byte) asLong == asLong) {
                        visitor.writeByte(element.getAsByte());
                    } else if ((short) asLong == asLong) {
                        visitor.writeShort(element.getAsShort());
                    } else if ((int) asLong == asLong) {
                        visitor.writeInt(element.getAsInt());
                    } else {
                        visitor.writeLong(asLong);
                    }
                } catch (ArithmeticException bruh /* quite cringe java moment, why use an exception for this */) {
                    var asDouble = value.doubleValue();

                    if ((float) asDouble == asDouble) {
                        visitor.writeFloat(element.getAsFloat());
                    } else {
                        visitor.writeDouble(asDouble);
                    }
                }
            }
        } else if (element instanceof JsonArray array) {
            try (var sequence = visitor.sequence(Endec.<JsonElement>of(this::decodeValue, deserializer -> null), array.size())) {
                array.forEach(sequence::element);
            }
        } else if (element instanceof JsonObject object) {
            try (var map = visitor.map(Endec.<JsonElement>of(this::decodeValue, deserializer -> null), object.size())) {
                object.asMap().forEach(map::entry);
            }
        } else {
            throw new IllegalArgumentException("Non-standard, unrecognized JsonElement implementation cannot be decoded");
        }
    }

    // ---

    private class Sequence<V> implements Deserializer.Sequence<V> {

        private final Endec<V> valueEndec;
        private final Iterator<JsonElement> elements;
        private final int size;

        private Sequence(Endec<V> valueEndec, JsonArray elements) {
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
                    () -> this.valueEndec.decode(JsonDeserializer.this),
                    false
            );
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final Endec<V> valueEndec;
        private final Iterator<java.util.Map.Entry<String, JsonElement>> entries;
        private final int size;

        private Map(Endec<V> valueEndec, JsonObject entries) {
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
                    () -> java.util.Map.entry(entry.getKey(), this.valueEndec.decode(JsonDeserializer.this)),
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
        public <F> @NotNull F field(String name, Endec<F> endec) {
            if (!this.object.has(name)) {
                throw new IllegalStateException("Field '" + name + "' was missing from serialized data, but no default value was provided");
            }
            return JsonDeserializer.this.frame(
                    () -> this.object.get(name),
                    () -> endec.decode(JsonDeserializer.this),
                    true
            );
        }

        @Override
        public <F> @Nullable F field(String name, Endec<F> endec, @Nullable F defaultValue) {
            if (!this.object.has(name)) return defaultValue;
            return JsonDeserializer.this.frame(
                    () -> this.object.get(name),
                    () -> endec.decode(JsonDeserializer.this),
                    true
            );
        }
    }
}
