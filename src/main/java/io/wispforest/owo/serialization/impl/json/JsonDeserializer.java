package io.wispforest.owo.serialization.impl.json;

import com.google.gson.*;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;

public class JsonDeserializer implements SelfDescribedDeserializer<JsonElement> {

    private final SerializationAttribute extraAttribute;

    protected final Deque<Supplier<JsonElement>> stack = new ArrayDeque<>();

    public JsonDeserializer(JsonElement element, boolean compressed) {
        stack.push(() -> element);

        extraAttribute = compressed ? SerializationAttribute.COMPRESSED : SerializationAttribute.HUMAN_READABLE;
    }

    private JsonElement topElement() {
        return stack.peek().get();
    }

    //--

    public static JsonDeserializer of(JsonElement element){
        return new JsonDeserializer(element, false);
    }

    public static JsonDeserializer compressed(JsonElement element){
        return new JsonDeserializer(element, true);
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
    public Object readAny() {
        var element = topElement();

        if (element instanceof JsonNull) return null;

        if (element instanceof JsonPrimitive primitive) {
            if (primitive.isString()) return element.getAsString();
            if (primitive.isBoolean()) return element.getAsBoolean();

            BigDecimal value = primitive.getAsBigDecimal();

            try {
                long l = value.longValueExact();

                if ((byte) l == l) return element.getAsByte();
                if ((short) l == l) return element.getAsShort();
                if ((int) l == l) return element.getAsInt();

                return element.getAsLong();
            } catch (ArithmeticException var10) {
                double d = value.doubleValue();

                if ((float) d == d) return element.getAsFloat();

                return d;
            }
        }

        if (element instanceof JsonArray array) {
            List<Object> objects = new ArrayList<>();

            array.forEach(element1 -> {
                stack.push(() -> element1);

                objects.add(readAny());

                stack.pop();
            });

            return objects;
        }

        if (element instanceof JsonObject object) {
            Map<String, Object> maps = new LinkedHashMap<>();

            object.asMap().forEach((s, element1) -> {
                stack.push(() -> element1);

                maps.put(s, readAny());

                stack.pop();
            });

            return maps;
        }

        throw new IllegalStateException("Unknown JsonElement Object: " + element);
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
    public <E> SequenceDeserializer<E> sequence(Endec<E> elementEndec) {
        return new JsonSequenceDeserializer<>(((JsonArray) topElement()).asList(), elementEndec);
    }

    @Override
    public <V> MapDeserializer<V> map(Endec<V> valueEndec) {
        return new JsonMapDeserializer<>(((JsonObject) topElement()).asMap(), valueEndec);
    }

    @Override
    public StructDeserializer struct() {
        return new JsonStructDeserializer(((JsonObject) topElement()).asMap());
    }

    public class JsonSequenceDeserializer<V> implements SequenceDeserializer<V> {

        private final Iterator<JsonElement> entries;
        private final int maxSize;

        private final Endec<V> valueEndec;

        public JsonSequenceDeserializer(List<JsonElement> entries, Endec<V> valueEndec) {
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

            V entry;

            try {
                entry = valueEndec.decode(JsonDeserializer.this);
            } finally {
                JsonDeserializer.this.stack.pop();
            }

            return entry;
        }
    }

    public class JsonMapDeserializer<V> implements MapDeserializer<V> {

        private final Iterator<Map.Entry<String, JsonElement>> entries;
        private final int maxSize;

        private final Endec<V> valueEndec;

        public JsonMapDeserializer(Map<String, JsonElement> map, Endec<V> valueEndec) {
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
        public Map.Entry<String, V> next() {
            var entry = entries.next();

            JsonDeserializer.this.stack.push(entry::getValue);

            Map.Entry<String, V> value;

            try {
                value = Map.entry(entry.getKey(), valueEndec.decode(JsonDeserializer.this));
            } finally {
                JsonDeserializer.this.stack.pop();
            }

            return value;
        }
    }

    public class JsonStructDeserializer implements StructDeserializer {

        private final Map<String, JsonElement> map;

        public JsonStructDeserializer(Map<String, JsonElement> map) {
            this.map = map;
        }

        @Override
        public <F> F field(String field, Endec<F> endec, @Nullable F defaultValue) {
            if (!map.containsKey(field)) return defaultValue;

            JsonDeserializer.this.stack.push(() -> map.get(field));

            F value;

            try {
                value = endec.decode(JsonDeserializer.this);
            } finally {
                JsonDeserializer.this.stack.pop();
            }

            return value;
        }
    }
}
