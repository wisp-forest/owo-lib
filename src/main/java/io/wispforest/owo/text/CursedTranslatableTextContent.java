package io.wispforest.owo.text;

import io.wispforest.owo.mixin.text.TranslatableTextContentAccessor;
import net.minecraft.text.*;

import java.util.ArrayList;

public class CursedTranslatableTextContent extends TranslatableTextContent {
    public static int argIndex = 0;

    private static final CursedTranslatableTextContent INSTANCE = new CursedTranslatableTextContent();

    private CursedTranslatableTextContent() {
        super("", null, null);
    }

    public static Text unpackArgs(Text text) {
        argIndex = 0;
        var returned = unpack(text);
        argIndex = 0;
        return returned;
    }

    private static Text unpack(Text text) {
        var unpacked = new ArrayList<Text>();
        TextContent newContent = PlainTextContent.EMPTY;
        if (text.getContent() instanceof PlainTextContent.Literal(String string)) {
            ((TranslatableTextContentAccessor) INSTANCE).owo$forEachPart(
                string,
                part -> {
                    if (part instanceof Text textPart) unpacked.add(textPart);
                    else unpacked.add(Text.literal(part.getString()));
                }
            );
        } else {
            if (text.getSiblings().isEmpty()) return text;
            newContent = text.getContent();
        }
        var newText = MutableText.of(newContent).setStyle(text.getStyle());
        for (var part : unpacked) newText.append(part);
        for (var child : text.getSiblings()) newText.append(unpack(child));
        return newText;
    }
}
