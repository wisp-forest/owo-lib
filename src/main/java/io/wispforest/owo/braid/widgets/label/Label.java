package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Clip;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class Label extends StatelessWidget {

    public final @Nullable LabelStyle style;
    public final boolean softWrap;
    public final Overflow overflow;
    public final Text text;

    public Label(@Nullable LabelStyle style, boolean softWrap, Overflow overflow, Text text) {
        this.style = style;
        this.softWrap = softWrap;
        this.overflow = overflow;
        this.text = text;
    }

    public Label(@Nullable LabelStyle style, boolean softWrap, Text text) {
        this(style, softWrap, Overflow.CLIP, text);
    }

    public Label(boolean softWrap, Text text) {
        this(null, softWrap, text);
    }

    public Label(Overflow overflow, Text text) {
        this(null, true, overflow, text);
    }

    public Label(Text text) {
        this(true, text);
    }

    public static Label literal(String text) {
        return new Label(Text.literal(text));
    }

    @Override
    public Widget build(BuildContext context) {
        var effectiveStyle = this.style != null ? this.style : LabelStyle.EMPTY;
        if (DefaultLabelStyle.maybeOf(context) instanceof LabelStyle contextStyle) {
            effectiveStyle = effectiveStyle.overriding(contextStyle);
        }

        Widget result = new RawLabel(
            effectiveStyle.fillDefaults(),
            this.softWrap,
            this.overflow == Overflow.ELLIPSIS,
            this.text
        );

        if (this.overflow == Overflow.CLIP) {
            result = new Clip(result);
        }

        return result;
    }

    public enum Overflow {
        SHOW, CLIP, ELLIPSIS
    }
}
