package io.wispforest.owo.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.util.Optional;

public record InsertingTextContent(int index) implements TextContent {

    public static final TextContent.Type<InsertingTextContent> TYPE = new Type<>(
            RecordCodecBuilder.mapCodec(
                    instance -> instance.group(Codec.INT.fieldOf("index").forGetter(InsertingTextContent::index)).apply(instance, InsertingTextContent::new)
            ),
            "owo:insert"
    );

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        var current = TranslationContext.getCurrent();

        if (current == null || current.getArgs().length <= index) {return visitor.accept("%" + (index + 1) + "$s");}

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

        if (current == null || current.getArgs().length <= index) {
            return visitor.accept(style, "%" + (index + 1) + "$s");
        }

        Object arg = current.getArgs()[index];

        if (arg instanceof Text text) {
            return text.visit(visitor, style);
        } else {
            return visitor.accept(style, arg.toString());
        }
    }

    @Override
    public Type<?> getType() {
        return TYPE;
    }
}
