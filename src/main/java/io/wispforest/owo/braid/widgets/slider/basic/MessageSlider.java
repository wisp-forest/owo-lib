package io.wispforest.owo.braid.widgets.slider.basic;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Stack;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class MessageSlider extends StatelessWidget {

    public final double value;
    public final double min;
    public final double max;
    public final @Nullable Double step;
    public final LayoutAxis axis;

    public final DoubleConsumer onChanged;
    public final Text message;

    public MessageSlider(double value, double min, double max, @Nullable Double step, LayoutAxis axis, DoubleConsumer onChanged, Text message) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.axis = axis;
        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageSlider(double value, DoubleConsumer onChanged, Text message, LayoutAxis axis) {
        this(value, 0, 1, null, axis, onChanged, message);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Slider(
                this.value,
                this.min,
                this.max,
                this.step,
                this.axis,
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
