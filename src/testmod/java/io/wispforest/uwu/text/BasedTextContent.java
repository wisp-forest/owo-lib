package io.wispforest.uwu.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextContent;

import java.util.Optional;

public class BasedTextContent implements TextContent {

    public static final Type<BasedTextContent> TYPE = new Type<>(RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.STRING.fieldOf("based").forGetter(o -> o.basedText)).apply(instance, BasedTextContent::new)
    ), "uwu:based");

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
    public Type<?> getType() {
        return TYPE;
    }
}
