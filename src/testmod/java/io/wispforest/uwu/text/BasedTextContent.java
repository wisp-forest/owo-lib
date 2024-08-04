package io.wispforest.uwu.text;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class BasedTextContent implements ComponentContents {

    public static final Type<BasedTextContent> TYPE = new Type<>(
            CodecUtils.toMapCodec(StructEndecBuilder.of(Endec.STRING.fieldOf("based", o -> o.basedText), BasedTextContent::new)),
            "uwu:based");

    private final String basedText;

    public BasedTextContent(String basedText) {
        this.basedText = basedText;
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
        return visitor.accept("I am extremely based: " + basedText);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> visitor, Style style) {
        return visitor.accept(style, "I am extremely based: " + basedText);
    }

    @Override
    public Type<?> type() {
        return TYPE;
    }
}
