package io.wispforest.owo.braid.widgets.slider.range;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.braid.widgets.slider.slider.SliderFunction;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

public class RangeSlider extends RawRangeSlider {

    public RangeSlider(
        double minValue,
        double maxValue,
        @Nullable WidgetSetupCallback<RangeSlider> setupCallback,
        @Nullable RangeSliderCallback onChanged
    ) {
        super(
            minValue,
            maxValue,
            null,
            onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            new DefaultSliderHandle(), 8,
            new DefaultSliderHandle(), 8,
            new Box(new Color(0x7f000000))
        );
        if (setupCallback != null) setupCallback.setup(this);
    }

    public RangeSlider(
        double minValue,
        double maxValue,
        @Nullable WidgetSetupCallback<RangeSlider> setupCallback,
        boolean active,
        RangeSliderCallback onChanged
    ) {
        this(minValue, maxValue, setupCallback, active ? onChanged : null);
    }

    @Override
    public RangeSlider min(double min) {
        return (RangeSlider) super.min(min);
    }

    @Override
    public RangeSlider max(double max) {
        return (RangeSlider) super.max(max);
    }

    @Override
    public RangeSlider range(double min, double max) {
        return (RangeSlider) super.range(min, max);
    }

    @Override
    public RangeSlider minRange(double minRange) {
        return (RangeSlider) super.minRange(minRange);
    }

    @Override
    public RangeSlider maxRange(double maxRange) {
        return (RangeSlider) super.maxRange(maxRange);
    }

    @Override
    public RangeSlider clampRange(double minRange, double maxRange) {
        return (RangeSlider) super.clampRange(minRange, maxRange);
    }

    @Override
    public RangeSlider step(@Nullable Double step) {
        return (RangeSlider) super.step(step);
    }

    @Override
    public RangeSlider step(double step) {
        return (RangeSlider) super.step(step);
    }

    @Override
    public RangeSlider sliderFunction(SliderFunction function) {
        return (RangeSlider) super.sliderFunction(function);
    }

    @Override
    public RangeSlider axis(LayoutAxis axis) {
        return (RangeSlider) super.axis(axis);
    }

    @Override
    public RangeSlider vertical() {
        return (RangeSlider) super.vertical();
    }

    @Override
    public RawRangeSlider incrementStep(double incrementStep) {
        return super.incrementStep(incrementStep);
    }

    public RangeSlider minHandleSize(double size) {
        this.assertMutable();
        this.minHandleSize = size;
        return this;
    }

    public double minHandleSize() {
        return this.minHandleSize;
    }

    public RangeSlider maxHandleSize(double size) {
        this.assertMutable();
        this.maxHandleSize = size;
        return this;
    }

    public double maxHandleSize() {
        return this.maxHandleSize;
    }
}
