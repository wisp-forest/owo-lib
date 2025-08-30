package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.braid.widgets.slider.ValueMapper;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

public class Slider extends RawSlider {

    public Slider(
        double value,
        @Nullable SliderSetupCallback<Slider> setupCallback,
        @Nullable SliderCallback onChanged
    ) {
        super(
            value,
            null,
            onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            8,
            new DefaultSliderHandle()
        );
        if (setupCallback != null) setupCallback.setup(this);
    }

    public Slider(
        double value,
        @Nullable SliderSetupCallback<Slider> setupCallback,
        SliderCallback onChanged,
        boolean active
    ) {
        this(value, setupCallback, active ? onChanged : null);
    }

    //region Setup Methods

    @Override
    public Slider min(double min) {
        return (Slider) super.min(min);
    }

    @Override
    public Slider max(double max) {
        return (Slider) super.max(max);
    }

    @Override
    public Slider range(double min, double max) {
        return (Slider) super.range(min, max);
    }

    @Override
    public Slider step(@Nullable Double step) {
        return (Slider) super.step(step);
    }

    @Override
    public Slider step(double step) {
        return (Slider) super.step(step);
    }

    @Override
    public Slider valueMapper(ValueMapper valueMapper) {
        return (Slider) super.valueMapper(valueMapper);
    }

    @Override
    public Slider axis(LayoutAxis axis) {
        return (Slider) super.axis(axis);
    }

    @Override
    public Slider horizontal() {
        return (Slider) super.horizontal();
    }

    @Override
    public Slider vertical() {
        return (Slider) super.vertical();
    }

    @Override
    public RawSlider incrementStep(double incrementStep) {
        return super.incrementStep(incrementStep);
    }

    public Slider handleSize(double handleSize) {
        this.assertMutable();
        this.handleSize = handleSize;
        return this;
    }

    public double handleSize() {
        return this.handleSize;
    }

    //TODO: do we let the handle/track be changed?

    //endregion
}
