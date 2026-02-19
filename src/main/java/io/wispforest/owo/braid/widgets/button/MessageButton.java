package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class MessageButton extends StatelessWidget {

    public final Component text;
    public final @Nullable Runnable onClick;

    public MessageButton(Component text, @Nullable Runnable onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    public MessageButton(Component text, boolean active, Runnable onClick) {
        this(text, active ? onClick : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Button(
            this.onClick,
            //TODO: abstract away the million places where a ternary operator is used to determine the label style for a possibly disabled button
            new Label(
                this.onClick != null
                    ? LabelStyle.SHADOW
                    : new LabelStyle(null, Color.formatting(ChatFormatting.GRAY), null, false),
                true,
                this.text
            )
        );
    }
}
