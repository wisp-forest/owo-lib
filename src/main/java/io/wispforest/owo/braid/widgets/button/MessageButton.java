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

import java.util.function.IntPredicate;

public class MessageButton extends StatelessWidget {

    public final Text text;
    public final @Nullable Runnable onClick;

    public MessageButton(Text text, @Nullable Runnable onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    public MessageButton(Text text, boolean active, @Nullable Runnable onClick) {
        this(text, active ? onClick : null);
    }

    @Override
    public Widget build(BuildContext context) {
        var active = this.onClick != null;
        return new Button(
            this.onClick,
            new Label(
                active
                    ? LabelStyle.SHADOW
                    : new LabelStyle(null, Color.ofFormatting(Formatting.GRAY), null, false),
                true,
                this.text
            )
        );
    }
}
