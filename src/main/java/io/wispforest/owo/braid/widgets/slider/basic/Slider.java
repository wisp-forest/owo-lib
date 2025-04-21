package io.wispforest.owo.braid.widgets.slider.basic;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class Slider extends StatelessWidget {

    public final double value;
    public final double min;
    public final double max;
    public final @Nullable Double step;
    public final LayoutAxis axis;

    public final DoubleConsumer onChanged;

    public Slider(double value, double min, double max, @Nullable Double step, LayoutAxis axis, DoubleConsumer onChanged) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.axis = axis;
        this.onChanged = onChanged;
    }

    public Slider(double value, DoubleConsumer onChanged, LayoutAxis axis) {
        this(value, 0, 1, null, axis, onChanged);
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawSlider(
            this.value,
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
