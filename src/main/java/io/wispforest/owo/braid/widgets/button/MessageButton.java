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

/// The braid equivalent of [net.minecraft.client.gui.widget.ButtonWidget]
///
/// @see Button
/// @see Label
public class MessageButton extends StatelessWidget {

    public final Text text;
    /// The callback to invoke when this widget is clicked.<br>
    /// `null` indicates this widget is inactive
    public final @Nullable Runnable onClick;

    public MessageButton(Text text, @Nullable Runnable onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    /// Create a `MessageButton` with an explicit active state
    public MessageButton(Text text, Runnable onClick, boolean active) {
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
