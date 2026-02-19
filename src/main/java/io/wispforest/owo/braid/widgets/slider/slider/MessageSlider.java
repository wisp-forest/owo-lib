package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class MessageSlider extends StatelessWidget {

    public final double value;
    public final @Nullable WidgetSetupCallback<Slider> setupCallback;
    public final @Nullable SliderCallback onChanged;

    public final Component message;

    public MessageSlider(
        double value,
        Component message,
        @Nullable WidgetSetupCallback<Slider> setupCallback,
        @Nullable SliderCallback onChanged
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageSlider(
        double value,
        Component message,
        @Nullable WidgetSetupCallback<Slider> setupCallback,
        boolean active,
        SliderCallback onChanged
    ) {
        this(value, message, setupCallback, active ? onChanged : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Slider(
                this.value,
                this.setupCallback, this.onChanged
            ),
            //TODO: abstract this styling?
            new Label(
                LabelStyle.SHADOW,
                false,
                this.message
            )
        );
    }
}
