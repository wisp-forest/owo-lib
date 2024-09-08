package io.wispforest.owo.text;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Text;

import java.util.Optional;

public record InsertingContents(int index) implements ComponentContents {

    public static final ComponentContents.Type<InsertingContents> TYPE = new Type<>(
            CodecUtils.toMapCodec(StructEndecBuilder.of(Endec.INT.fieldOf("index", InsertingContents::index), InsertingContents::new)),
            "owo:insert"
    );

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
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
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> visitor, Style style) {
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
    public Type<?> type() {
        return TYPE;
    }
}
