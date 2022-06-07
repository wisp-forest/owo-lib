package io.wispforest.owo.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface CustomTextContentSerializer<T extends CustomTextContent> {

    T deserialize(JsonObject obj, JsonDeserializationContext ctx);

    void serialize(T content, JsonObject obj, JsonSerializationContext ctx);
}
