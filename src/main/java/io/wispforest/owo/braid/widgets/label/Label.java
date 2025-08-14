package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/// A widget that displays a [Text].<br>
/// Supports all functionality of [Style], including hover and click events.<br>
/// Displayed text can be soft-wrapped and given additional styling via [#style].
public class Label extends StatelessWidget {

    /// The LabelStyle to apply to this label.<br>
    /// Not to be confused with the [Style] of the [#text] itself.
    public final @Nullable LabelStyle style;
    public final boolean softWrap;
    public final Text text;

    public Label(@Nullable LabelStyle style, boolean softWrap, Text text) {
        this.style = style;
        this.softWrap = softWrap;
        this.text = text;
    }

    /// Create a `Label` with the default `LabelStyle` ([LabelStyle#EMPTY])
    public Label(boolean softWrap, Text text) {
        this(null, softWrap, text);
    }

    /// Create a `Label` with the default `LabelStyle` ([LabelStyle#EMPTY]) and soft wrapping enabled
    public Label(Text text) {
        this(null, true, text);
    }

    /// Create a `Label` from a plain text string.<br>
    /// Uses the default `LabelStyle` ([LabelStyle#EMPTY]) and will soft-wrap
    public static Label literal(String text) {
        return new Label(Text.literal(text));
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
