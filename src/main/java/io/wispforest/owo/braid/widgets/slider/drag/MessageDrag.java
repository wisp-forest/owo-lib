package io.wispforest.owo.braid.widgets.slider.drag;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.slider.slider.SliderCallback;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class MessageDrag extends StatelessWidget {

    public final double value;
    public final @Nullable WidgetSetupCallback<Drag> setupCallback;
    public final @Nullable SliderCallback onChanged;

    public final Component message;

    public MessageDrag(
        double value,
        @Nullable WidgetSetupCallback<Drag> setupCallback,
        @Nullable SliderCallback onChanged,
        Component message
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageDrag(
        double value,
        @Nullable WidgetSetupCallback<Drag> setupCallback,
        boolean active,
        SliderCallback onChanged,
        Component message
    ) {
        this(value, setupCallback, active ? onChanged : null, message);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Drag(
                this.value,
                this.setupCallback,
                this.onChanged
            ),
            new Label(
                LabelStyle.SHADOW,
                false,
                this.message
            )
        );
    }
}
