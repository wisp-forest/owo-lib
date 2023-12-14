package io.wispforest.owo.text;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.util.Optional;

public record InsertingTextContent(int index) implements TextContent {

    public static final TextContent.Type<InsertingTextContent> TYPE = new Type<>(
            StructEndecBuilder.of(Endec.INT.fieldOf("index", InsertingTextContent::index), InsertingTextContent::new).mapCodec(),
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
