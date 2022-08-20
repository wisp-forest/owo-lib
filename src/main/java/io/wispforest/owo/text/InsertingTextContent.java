package io.wispforest.owo.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

import java.util.Optional;

public record InsertingTextContent(int index) implements CustomTextContent {
    public static void init() {
        CustomTextRegistry.register("index", Serializer.INSTANCE);
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        var current = TranslationContext.getCurrent();

        if (current == null || current.getArgs().length <= index)
            return visitor.accept("%" + (index + 1) + "$s");

        Object arg = current.getArgs()[index];

        if (arg instanceof Text text) {
            return text.visit(visitor);
        } else {
            return visitor.accept(arg.toString());
        }
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        var current = TranslationContext.getCurrent();

        if (current == null || current.getArgs().length <= index)
            return visitor.accept(style, "%" + (index + 1) + "$s");

        Object arg = current.getArgs()[index];

        if (arg instanceof Text text) {
            return text.visit(visitor, style);
        } else {
            return visitor.accept(style, arg.toString());
        }
    }

    @Override
    public CustomTextContentSerializer<?> serializer() {
        return Serializer.INSTANCE;
    }

    private enum Serializer implements CustomTextContentSerializer<InsertingTextContent> {
        INSTANCE;

        @Override
        public InsertingTextContent deserialize(JsonObject obj, JsonDeserializationContext ctx) {
            return new InsertingTextContent(JsonHelper.getInt(obj, "index"));
        }

        @Override
        public void serialize(InsertingTextContent content, JsonObject obj, JsonSerializationContext ctx) {
            obj.addProperty("index", content.index);
        }
    }
}
