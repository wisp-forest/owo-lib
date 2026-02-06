package io.wispforest.owo.text;

import io.wispforest.owo.mixin.text.TranslatableContentsAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;

public class CursedTranslatableContents extends TranslatableContents {
    public static int argIndex = 0;

    private static final CursedTranslatableContents INSTANCE = new CursedTranslatableContents();

    private CursedTranslatableContents() {
        super("", null, null);
    }

    public static Component unpackArgs(Component text) {
        argIndex = 0;
        var returned = unpack(text);
        argIndex = 0;
        return returned;
    }

    private static Component unpack(Component text) {
        var unpacked = new ArrayList<Component>();
        ComponentContents newContent = PlainTextContents.EMPTY;
        if (text.getContents() instanceof PlainTextContents.LiteralContents(String string)) {
            ((TranslatableContentsAccessor) INSTANCE).owo$decomposeTemplate(
                string,
                part -> {
                    if (part instanceof Component textPart) unpacked.add(textPart);
                    else unpacked.add(Component.literal(part.getString()));
                }
            );
        } else {
            if (text.getSiblings().isEmpty()) return text;
            newContent = text.getContents();
        }
        var newText = MutableComponent.create(newContent).setStyle(text.getStyle());
        for (var part : unpacked) newText.append(part);
        for (var child : text.getSiblings()) newText.append(unpack(child));
        return newText;
    }
}
