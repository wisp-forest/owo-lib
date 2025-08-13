package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/// A [Button] with a [Label] as its child
/// @see Button
/// @see Label
/// @author glisco
/// @author chyzman
public class MessageButton extends StatelessWidget {

    /// The [Text] displayed on this [MessageButton]
    public final Text text;
    /// The Runnable called when this [MessageButton] is clicked
    ///
    /// If `null`, disables this [MessageButton]
    public final @Nullable Runnable onClick;

    public MessageButton(Text text, @Nullable Runnable onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    public MessageButton(Text text, boolean active, Runnable onClick) {
        this(text, active ? onClick : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Button(
            this.onClick,
            new Label(
                this.onClick != null
                    ? LabelStyle.SHADOW
                    : new LabelStyle(null, Color.ofFormatting(Formatting.GRAY), null, false),
                true,
                this.text
            )
        );
    }
}
