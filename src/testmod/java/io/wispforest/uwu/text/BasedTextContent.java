package io.wispforest.uwu.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.wispforest.owo.text.CustomTextContent;
import io.wispforest.owo.text.CustomTextContentSerializer;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.JsonHelper;

import java.util.Optional;

public class BasedTextContent implements CustomTextContent {
    private final String basedText;

    public BasedTextContent(String basedText) {
        this.basedText = basedText;
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return visitor.accept("I am extremely based: " + basedText);
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return visitor.accept(style, "I am extremely based: " + basedText);
    }

    @Override
    public CustomTextContentSerializer<?> serializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements CustomTextContentSerializer<BasedTextContent> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public BasedTextContent deserialize(JsonObject obj, JsonDeserializationContext ctx) {
            return new BasedTextContent(JsonHelper.getString(obj, "based"));
        }

        @Override
        public void serialize(BasedTextContent content, JsonObject obj, JsonSerializationContext ctx) {
            obj.addProperty("based", content.basedText);
        }
    }
}
