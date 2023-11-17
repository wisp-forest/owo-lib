package io.wispforest.owo.serialization.impl.json;

import com.google.gson.*;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonDeserializer implements SelfDescribedDeserializer<JsonElement> {

    private static final Set<SerializationAttribute> ATTRIBUTES = EnumSet.allOf(SerializationAttribute.class);

    protected final Deque<Supplier<JsonElement>> stack = new ArrayDeque<>();

    public JsonDeserializer(JsonElement element, boolean compressed) {
        stack.push(() -> element);
    }

    private JsonElement topElement() {
        return stack.peek().get();
    }

    //--

    public static JsonDeserializer of(JsonElement element) {
        return new JsonDeserializer(element, false);
    }

    public static JsonDeserializer compressed(JsonElement element) {
        return new JsonDeserializer(element, true);
    }

    @Override
    public Set<SerializationAttribute> attributes() {
        return ATTRIBUTES;
    }

    //--

    @Override
    public <S> void readAny(Serializer<S> visitor) {
        this.decodeValue(visitor, this.topElement());
    }

    private <S> void decodeValue(Serializer<S> visitor, JsonElement element) {
        if (element.isJsonNull()) {
            visitor.writeOptional(Endec.JSON_ELEMENT, Optional.empty());
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

    //--

    @Override
    public <V> Optional<V> readOptional(Endec<V> endec) {
        var element = topElement();

        if (element.isJsonNull()) return Optional.empty();

        return Optional.of(endec.decode(this));
    }

    @Override
    public boolean readBoolean() {
        return topElement().getAsBoolean();
    }

    @Override
    public byte readByte() {
        return topElement().getAsByte();
    }

    @Override
    public short readShort() {
        return topElement().getAsShort();
    }

    @Override
    public int readInt() {
        return topElement().getAsInt();
    }

    @Override
    public long readLong() {
        return topElement().getAsLong();
    }

    @Override
    public float readFloat() {
        return topElement().getAsFloat();
    }

    @Override
    public double readDouble() {
        return topElement().getAsDouble();
    }

    @Override
    public String readString() {
        return topElement().getAsString();
    }

    @Override
    public byte[] readBytes() {
        var jsonArray = topElement().getAsJsonArray().asList();

        byte[] array = new byte[topElement().getAsJsonArray().size()];

        for (int i = 0; i < jsonArray.size(); i++) array[i] = jsonArray.get(i).getAsByte();

        return array;
        //return Base64.getDecoder().decode(topElement().getAsString());
    }

    @Override
    public int readVarInt() {
        return readInt();
    }

    @Override
    public long readVarLong() {
        return readLong();
    }

    @Override
    public <V> V tryRead(Function<Deserializer<JsonElement>, V> func) {
        var stackCopy = new ArrayList<>(stack);

        try {
            return func.apply(this);
        } catch (Exception e) {
            stack.clear();
            stack.addAll(stackCopy);

            throw e;
        }
    }

    @Override
    public <E> Deserializer.Sequence<E> sequence(Endec<E> elementEndec) {
        return new Sequence<>(((JsonArray) topElement()).asList(), elementEndec);
    }

    @Override
    public <V> Deserializer.Map<V> map(Endec<V> valueEndec) {
        return new Map<>(((JsonObject) topElement()).asMap(), valueEndec);
    }

    @Override
    public Deserializer.Struct struct() {
        return new Struct(((JsonObject) topElement()).asMap());
    }

    private class Sequence<V> implements Deserializer.Sequence<V> {

        private final Iterator<JsonElement> entries;
        private final int maxSize;

        private final Endec<V> valueEndec;

        private Sequence(List<JsonElement> entries, Endec<V> valueEndec) {
            this.entries = entries.iterator();
            this.maxSize = entries.size();

            this.valueEndec = valueEndec;
        }

        @Override
        public int size() {
            return maxSize;
        }

        @Override
        public boolean hasNext() {
            return entries.hasNext();
        }

        @Override
        public V next() {
            JsonDeserializer.this.stack.push(entries::next);

            V entry = valueEndec.decode(JsonDeserializer.this);

            JsonDeserializer.this.stack.pop();

            return entry;
        }
    }

    private class Map<V> implements Deserializer.Map<V> {

        private final Iterator<java.util.Map.Entry<String, JsonElement>> entries;
        private final int maxSize;

        private final Endec<V> valueEndec;

        private Map(java.util.Map<String, JsonElement> map, Endec<V> valueEndec) {
            this.entries = map.entrySet().iterator();
            this.maxSize = map.size();

            this.valueEndec = valueEndec;
        }

        @Override
        public int size() {
            return maxSize;
        }

        @Override
        public boolean hasNext() {
            return entries.hasNext();
        }

        @Override
        public java.util.Map.Entry<String, V> next() {
            var entry = entries.next();

            JsonDeserializer.this.stack.push(entry::getValue);

            java.util.Map.Entry<String, V> value = java.util.Map.entry(entry.getKey(), valueEndec.decode(JsonDeserializer.this));

            JsonDeserializer.this.stack.pop();

            return value;
        }
    }

    private class Struct implements Deserializer.Struct {

        private final java.util.Map<String, JsonElement> map;

        private Struct(java.util.Map<String, JsonElement> map) {
            this.map = map;
        }

        @Override
        public <F> F field(String field, Endec<F> endec, @Nullable F defaultValue) {
            if (!map.containsKey(field)) return defaultValue;

            JsonDeserializer.this.stack.push(() -> map.get(field));

            F value = endec.decode(JsonDeserializer.this);

            JsonDeserializer.this.stack.pop();

            return value;
        }
    }
}
