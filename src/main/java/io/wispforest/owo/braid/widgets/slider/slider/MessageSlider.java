package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class MessageSlider extends StatelessWidget {

    public final double value;
    public final @Nullable WidgetSetupCallback<RawSlider> setupCallback;
    public final @Nullable SliderCallback onChanged;

    public final Text message;

    public MessageSlider(
        double value,
        @Nullable WidgetSetupCallback<RawSlider> setupCallback,
        @Nullable SliderCallback onChanged,
        Text message
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageSlider(
        double value,
        @Nullable WidgetSetupCallback<RawSlider> setupCallback,
        SliderCallback onChanged,
        boolean active,
        Text message
    ) {
        this(value, setupCallback, active ? onChanged : null, message);
    }


    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Slider(
                this.value,
                this.setupCallback,
                this.onChanged
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
