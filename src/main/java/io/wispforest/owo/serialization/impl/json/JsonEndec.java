package io.wispforest.owo.serialization.impl.json;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JsonEndec implements Endec<JsonElement> {

    public static JsonEndec INSTANCE = new JsonEndec();

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public <E> void encode(Serializer<E> serializer, JsonElement value) {
        if(serializer.attributes().contains(SerializationAttribute.SELF_DESCRIBING)){
            writeAny(serializer, value);

            return;
        }

        try {
            Endec.STRING.encode(serializer, value.toString());
        } catch (AssertionError e){
            LOGGER.error("Unable to serialize the given JsonElement into the given format!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public <E> JsonElement decode(Deserializer<E> deserializer) {
        if(deserializer instanceof SelfDescribedDeserializer<E> selfDescribedDeserializer){
            return toJson(selfDescribedDeserializer.readAny());
        }

        try {
            return new JsonStreamParser(Endec.STRING.decode(deserializer)).next();
        } catch (JsonParseException e){
            LOGGER.error("Unable to deserialize the given format into the desired JsonElement!");
            throw new RuntimeException(e);
        }
    }

    //--

    private void writeAny(Serializer serializer, JsonElement element){
        if (element instanceof JsonNull){
            serializer.writeOptional(this, Optional.empty());
        } else if (element instanceof JsonPrimitive primitive) {
            if (primitive.isString()) {
                serializer.writeString(primitive.getAsString());

                return;
            } else if (primitive.isBoolean()) {
                serializer.writeBoolean(element.getAsBoolean());

                return;
            }

            BigDecimal value = primitive.getAsBigDecimal();

            try {
                long l = value.longValueExact();

                if ((byte) l == l) {
                    serializer.writeByte(element.getAsByte());
                } else if ((short) l == l) {
                    serializer.writeShort(element.getAsShort());
                } else if ((int) l == l) {
                    serializer.writeInt(element.getAsInt());
                } else {
                    serializer.writeLong(element.getAsLong());
                }
            } catch (ArithmeticException var10) {
                double d = value.doubleValue();

                if ((float) d == d) {
                    serializer.writeFloat(element.getAsFloat());
                } else {
                    serializer.writeDouble(d);
                }
            }
        } else if (element instanceof JsonArray array) {
            list().encode(serializer, array.asList());
        } else if (element instanceof JsonObject object) {
            map().encode(serializer, object.asMap());
        } else {
            throw new IllegalStateException("Unknown JsonElement Object: " + element);
        }
    }

    private JsonElement toJson(Object object) {
        if (object == null) {
            return JsonNull.INSTANCE;
        } else if(object instanceof String value){
            return new JsonPrimitive(value);
        } else if(object instanceof Boolean value) {
            return new JsonPrimitive(value);
        } else if(object instanceof Byte value) {
            return new JsonPrimitive(value);
        } else if(object instanceof Short value) {
            return new JsonPrimitive(value);
        } else if(object instanceof Integer value) {
            return new JsonPrimitive(value);
        } else if(object instanceof Long value) {
            return new JsonPrimitive(value);
        } else if(object instanceof Float value) {
            return new JsonPrimitive(value);
        } else if(object instanceof Double value) {
            return new JsonPrimitive(value);
        } else if (object instanceof List objects) {
            JsonArray array = new JsonArray();

            for (Object o : objects) array.add(toJson(o));

            return array;
        } else if (object instanceof Map map) {
            JsonObject jsonObject = new JsonObject();

            map.forEach((key, value) -> jsonObject.add((String) key, toJson(value)));

            return jsonObject;
        } else {
            throw new IllegalStateException("Unknown Object type: " + object);
        }
    }
}
