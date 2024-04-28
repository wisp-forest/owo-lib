package io.wispforest.owo.serialization.format.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import io.wispforest.owo.serialization.*;

public final class JsonEndec implements Endec<JsonElement> {

    public static final JsonEndec INSTANCE = new JsonEndec();

    private JsonEndec() {}

    @Override
    public void encode(SerializationContext ctx, Serializer<?> serializer, JsonElement value) {
        if (serializer instanceof SelfDescribedSerializer<?>) {
            JsonDeserializer.of(value).readAny(ctx, serializer);
            return;
        }

        serializer.writeString(ctx, value.toString());
    }

    @Override
    public JsonElement decode(SerializationContext ctx, Deserializer<?> deserializer) {
        if (deserializer instanceof SelfDescribedDeserializer<?> selfDescribedDeserializer) {
            var json = JsonSerializer.of();
            selfDescribedDeserializer.readAny(ctx, json);

            return json.result();
        }

        return new JsonStreamParser(deserializer.readString(ctx)).next();
    }
}
