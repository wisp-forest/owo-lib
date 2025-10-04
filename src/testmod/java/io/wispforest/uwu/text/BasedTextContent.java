package io.wispforest.uwu.text;

import com.mojang.serialization.MapCodec;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextContent;

import java.util.Map;
import java.util.Optional;

public class BasedTextContent implements TextContent {

    public static final MapCodec<BasedTextContent> CODEC = CodecUtils.toMapCodec(StructEndecBuilder.of(Endec.STRING.fieldOf("based", o -> o.basedText), BasedTextContent::new));

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
    public MapCodec<? extends TextContent> getCodec() {
        return CODEC;
    }
}
