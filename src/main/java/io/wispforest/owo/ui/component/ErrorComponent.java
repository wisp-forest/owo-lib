package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.definitions.Sizing;
import io.wispforest.owo.ui.layout.ScrollContainer;
import net.minecraft.text.Text;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorComponent extends ScrollContainer<LabelComponent> {

    protected ErrorComponent(Sizing horizontalSizing, Sizing verticalSizing, LabelComponent child) {
        super(ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child);
    }

    public static ErrorComponent create(Throwable error) {
        var writer = new StringWriter();
        error.printStackTrace(new PrintWriter(writer));

        return new ErrorComponent(
                Sizing.fill(100),
                Sizing.fill(100),
                Components.label(Text.literal(writer.toString().replace("\t", "  "))).color(0xFF0000)
        );
    }

    public static ErrorComponent create(String error) {
        return new ErrorComponent(
                Sizing.fill(100),
                Sizing.fill(100),
                Components.label(Text.literal(error)).color(0xFF0000)
        );
    }
}
