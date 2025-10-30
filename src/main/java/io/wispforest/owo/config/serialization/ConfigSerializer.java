package io.wispforest.owo.config.serialization;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import io.wispforest.endec.*;
import io.wispforest.endec.format.jankson.JanksonDeserializer;
import io.wispforest.endec.format.jankson.JanksonEndec;
import io.wispforest.endec.format.jankson.JanksonSerializer;
import io.wispforest.owo.config.base.Key;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

///
/// Acts as a way to allow for other formats than Jankson for when saving to storage.
///
public abstract class ConfigSerializer<E> {

    private static final Map<Identifier, ConfigSerializer<?>> CONFIG_SERIALIZERS = new HashMap<>();

    public static final Endec<RawConfigData<?>> RAW_DATA_ENDEC = new StructEndec<RawConfigData<?>>() {
        @Override
        public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, RawConfigData<?> value) {
            encodeStructTyped(ctx, struct, value);
        }

        public <T> void encodeStructTyped(SerializationContext ctx, Serializer.Struct struct, RawConfigData<T> value) {
            struct.field("serializer", ctx, MinecraftEndecs.IDENTIFIER, value.serializer().id());
            struct.field("data", ctx, value.serializer().endec(), value.element());
        }

        @Override
        public RawConfigData<?> decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
            var id = struct.field("serializer", ctx, MinecraftEndecs.IDENTIFIER, null);

            var serializer = CONFIG_SERIALIZERS.get(id);

            if (serializer == null) throw new IllegalStateException("Unable to get the required ConfigSerializer '" + id + "' as non was found to be registered!");

            return decodeStructTyped(ctx, struct, serializer);
        }

        public <T> RawConfigData<T> decodeStructTyped(SerializationContext ctx, Deserializer.Struct struct, ConfigSerializer<T> serializer) {
            return new RawConfigData<>(serializer, struct.field("data", ctx, serializer.endec(), null));
        }
    };

    public static final ConfigSerializer<JsonElement> JANKSON = new ConfigSerializer<>(Identifier.of("owo", "jankson"), JanksonEndec.INSTANCE) {
        private final Jankson jankson = new Jankson.Builder().build();

        @Override
        public <T> String encodeToString(SerializationContext context, Endec<T> endec, T t) {
            return encodeToFormat(context, endec, t).toJson(JsonGrammar.JANKSON);
        }

        @Override
        public <T> JsonElement encodeToFormat(SerializationContext context, Endec<T> endec, T t) {
            return endec.encodeFully(context, JanksonSerializer::of, t);
        }

        @Override
        public <T> T decodeFromFormat(SerializationContext context, Endec<T> endec, JsonElement e) {
            return endec.decodeFully(JanksonDeserializer::of, e);
        }

        @Override
        public JsonElement decodeFromString(String str) throws Exception {
            return this.jankson.load(str);
        }

        @Override
        public JsonElement getElementForKey(JsonElement e, Key key) {
            if (!(e instanceof JsonObject obj)) {
                throw new IllegalStateException("Unable to get options element as the passed element is not a JsonObject!");
            }

            return obj.recursiveGet(JsonElement.class, key.asString());
        }
    };

    private final Endec<E> endec;
    private final Identifier id;

    public ConfigSerializer(Identifier id, Endec<E> endec) {
        if (CONFIG_SERIALIZERS.containsKey(id)) {
            throw new IllegalStateException("Unable to register the given ConfigSerializer as a existing instance shares the given id: " + id);
        }

        CONFIG_SERIALIZERS.put(id, this);

        this.id = id;
        this.endec = endec;
    }

    public abstract E getElementForKey(E e, Key key);

    public abstract <T> E encodeToFormat(SerializationContext context, Endec<T> endec, T t);

    public abstract <T> T decodeFromFormat(SerializationContext context, Endec<T> endec, E e);

    public <T> String encodeToString(SerializationContext context, Endec<T> endec, T t) {
        return encodeToFormat(context, endec, t).toString();
    }

    public <T> RawConfigData<E> encodeToRaw(SerializationContext context, Endec<T> endec, T t) {
        return new RawConfigData<>(this, encodeToFormat(context, endec, t));
    }

    public abstract E decodeFromString(String str) throws Exception;

    public RawConfigData<E> decodeToRaw(String str) throws Exception {
        return new RawConfigData<>(this, decodeFromString(str));
    }

    public Endec<E> endec() {
        return this.endec;
    }

    public Identifier id() {
        return this.id;
    }

    @Override
    public String toString() {
        return "ConfigSerializer[Id: " + id() + "]";
    }
}
