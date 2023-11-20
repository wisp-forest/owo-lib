package io.wispforest.owo.serialization.impl.json;

import com.google.gson.JsonObject;
import io.wispforest.owo.serialization.MapCarrier;
import io.wispforest.owo.serialization.impl.KeyedField;
import org.jetbrains.annotations.NotNull;

public interface JsonMapCarrier extends MapCarrier {

    default <T> T get(@NotNull KeyedField<T> key) {
        return key.endec.decodeFully(JsonDeserializer::of, getMap().get(key.name));
    }

    default <T> void put(@NotNull KeyedField<T> key, @NotNull T value) {
        getMap().add(key.name, key.endec.encodeFully(JsonSerializer::of, value));
    }

    @Override
    default <T> boolean has(@NotNull KeyedField<T> key){
        return getMap().has(key.name);
    }

    @Override
    default <T> void delete(@NotNull KeyedField<T> key){
        getMap().remove(key.name);
    }

    default JsonObject getMap() {
        throw new IllegalStateException("Interface default method called");
    }

    //--

    static JsonMapCarrier of(JsonObject object){
        return ((JsonMapCarrier)(Object) object);
    }
}
