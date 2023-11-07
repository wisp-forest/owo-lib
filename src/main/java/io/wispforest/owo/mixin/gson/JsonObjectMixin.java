package io.wispforest.owo.mixin.gson;

import com.google.gson.JsonObject;
import io.wispforest.owo.serialization.impl.json.JsonMapCarrier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(JsonObject.class)
public class JsonObjectMixin implements JsonMapCarrier {
    @Override
    public JsonObject getMap() {
        return (JsonObject) (Object) this;
    }
}
