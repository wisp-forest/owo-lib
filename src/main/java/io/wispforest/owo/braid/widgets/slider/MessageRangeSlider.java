package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class MessageRangeSlider extends StatelessWidget {

    public final double minValue, maxValue;
    public final double min;
    public final double max;
    public final @Nullable Double step;
    public final LayoutAxis axis;

    public final RawRangeSlider.RangeSliderCallback onChanged;
    public final Text message;

    public MessageRangeSlider(
        double minValue,
        double maxValue,
        double min,
        double max,
        @Nullable Double step,
        LayoutAxis axis,
        RawRangeSlider.RangeSliderCallback onChanged,
        Text message
    ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.min = min;
        this.max = max;
        this.step = step;
        this.axis = axis;
        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageRangeSlider(
        double minValue,
        double maxValue,
        RawRangeSlider.RangeSliderCallback onChanged,
        Text message,
        LayoutAxis axis
    ) {
        this(minValue, maxValue, 0, 1, null, axis, onChanged, message);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new RangeSlider(
                this.minValue,
                this.maxValue,
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

    @FunctionalInterface
    public interface RangeSliderMessageProvider {
        Text getMessage(double x, double y);
    }
}
