package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class Label extends StatelessWidget {

    public final @Nullable LabelStyle style;
    public final boolean softWrap;
    public final Text text;

    public Label(@Nullable LabelStyle style, boolean softWrap, Text text) {
        this.style = style;
        this.softWrap = softWrap;
        this.text = text;
    }

    public Label(boolean softWrap, Text text) {
        this(null, softWrap, text);
    }

    public Label(Text text) {
        this(null, true, text);
    }

    @Override
    public Widget build(BuildContext context) {
        var effectiveStyle = this.style != null ? this.style : LabelStyle.EMPTY;
        if (DefaultLabelStyle.maybeOf(context) instanceof LabelStyle contextStyle) {
            effectiveStyle = effectiveStyle.overriding(contextStyle);
        }

        return new RawLabel(
            effectiveStyle.fillDefaults(),
            this.softWrap,
            this.text
        );
    }
}
