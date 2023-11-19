package io.wispforest.owo.serialization.impl.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SelfDescribedDeserializer;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.impl.SerializationAttribute;

public final class JsonEndec implements Endec<JsonElement> {

    public static final JsonEndec INSTANCE = new JsonEndec();

    private JsonEndec() {}

    @Override
    public <E> void encode(Serializer<E> serializer, JsonElement value) {
        if (serializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING)) {
            JsonDeserializer.of(value).readAny(serializer);
            return;
        }

        serializer.writeString(value.toString());
    }

    @Override
    public <E> JsonElement decode(Deserializer<E> deserializer) {
        if (deserializer instanceof SelfDescribedDeserializer<E> selfDescribedDeserializer) {
            var json = JsonSerializer.of();
            selfDescribedDeserializer.readAny(json);

            return json.result();
        }

        return new JsonStreamParser(deserializer.readString()).next();
    }
}