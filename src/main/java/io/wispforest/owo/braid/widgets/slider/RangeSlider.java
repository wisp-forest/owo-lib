package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class RangeSlider extends StatelessWidget {

    public final double minValue, maxValue;
    public final double min;
    public final double max;
    public final @Nullable Double step;
    public final LayoutAxis axis;

    public final RawRangeSlider.RangeSliderCallback onChanged;

    public RangeSlider(
        double minValue,
        double maxValue,
        double min,
        double max,
        @Nullable Double step,
        LayoutAxis axis,
        RawRangeSlider.RangeSliderCallback onChanged
    ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.min = min;
        this.max = max;
        this.step = step;
        this.axis = axis;
        this.onChanged = onChanged;
    }

    public RangeSlider(double minValue, double maxValue, RawRangeSlider.RangeSliderCallback onChanged, LayoutAxis axis) {
        this(minValue, maxValue, 0, 1, null, axis, onChanged);
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawRangeSlider(
            this.minValue,
            this.maxValue,
            this.min,
            this.max,
            this.step,
            this.axis,
            this.onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            new DefaultSliderHandle(),
            8
        );
    }
}
