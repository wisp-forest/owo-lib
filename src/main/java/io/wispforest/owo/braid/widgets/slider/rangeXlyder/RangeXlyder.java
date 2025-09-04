package io.wispforest.owo.braid.widgets.slider.rangeXlyder;

import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.braid.widgets.slider.slider.SliderFunction;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

/**
 * A high-level slider widget with two handles that can each be independently dragged on both X and Y axes.
 * This is a convenience wrapper around RawRangeXlyder with default styling.
 */
public class RangeXlyder extends RawRangeXlyder {

    public RangeXlyder(
        Vector2dc minValue,
        Vector2dc maxValue,
        @Nullable WidgetSetupCallback<RangeXlyder> setupCallback,
        @Nullable RangeXlyderCallback onChanged
    ) {
        super(
            minValue,
            maxValue,
            null,
            onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            Size.square(8),
            new DefaultSliderHandle(),
            Size.square(8),
            new DefaultSliderHandle()
        );
        if (setupCallback != null) setupCallback.setup(this);
    }

    public RangeXlyder(
        Vector2dc minValue,
        Vector2dc maxValue,
        @Nullable WidgetSetupCallback<RangeXlyder> setupCallback,
        boolean active,
        RangeXlyderCallback onChanged
    ) {
        this(minValue, maxValue, setupCallback, active ? onChanged : null);
    }

    public RangeXlyder(
        double minX, double minY, double maxX, double maxY,
        @Nullable WidgetSetupCallback<RangeXlyder> setupCallback,
        @Nullable RangeXlyderCallback onChanged
    ) {
        this(new Vector2d(minX, minY), new Vector2d(maxX, maxY), setupCallback, onChanged);
    }

    public RangeXlyder(
        double minX, double minY, double maxX, double maxY,
        @Nullable WidgetSetupCallback<RangeXlyder> setupCallback,
        boolean active,
        RangeXlyderCallback onChanged
    ) {
        this(new Vector2d(minX, minY), new Vector2d(maxX, maxY), setupCallback, active ? onChanged : null);
    }

    //region Setup Methods - Override return types for method chaining

    @Override
    public RangeXlyder min(Vector2d min) {
        return (RangeXlyder) super.min(min);
    }

    @Override
    public RangeXlyder min(double minX, double minY) {
        return (RangeXlyder) super.min(minX, minY);
    }

    @Override
    public RangeXlyder min(double min) {
        return (RangeXlyder) super.min(min);
    }

    @Override
    public RangeXlyder minX(double minX) {
        return (RangeXlyder) super.minX(minX);
    }

    @Override
    public RangeXlyder minY(double minY) {
        return (RangeXlyder) super.minY(minY);
    }

    @Override
    public RangeXlyder max(Vector2d max) {
        return (RangeXlyder) super.max(max);
    }

    @Override
    public RangeXlyder max(double maxX, double maxY) {
        return (RangeXlyder) super.max(maxX, maxY);
    }

    @Override
    public RangeXlyder max(double max) {
        return (RangeXlyder) super.max(max);
    }

    @Override
    public RangeXlyder maxX(double maxX) {
        return (RangeXlyder) super.maxX(maxX);
    }

    @Override
    public RangeXlyder maxY(double maxY) {
        return (RangeXlyder) super.maxY(maxY);
    }

    @Override
    public RangeXlyder range(Vector2d min, Vector2d max) {
        return (RangeXlyder) super.range(min, max);
    }

    @Override
    public RangeXlyder range(double minX, double minY, double maxX, double maxY) {
        return (RangeXlyder) super.range(minX, minY, maxX, maxY);
    }

    @Override
    public RangeXlyder step(@Nullable Double step) {
        return (RangeXlyder) super.step(step);
    }

    @Override
    public RangeXlyder step(double step) {
        return (RangeXlyder) super.step(step);
    }

    @Override
    public RangeXlyder stepX(@Nullable Double xStep) {
        return (RangeXlyder) super.stepX(xStep);
    }

    @Override
    public RangeXlyder stepX(double xStep) {
        return (RangeXlyder) super.stepX(xStep);
    }

    @Override
    public RangeXlyder stepY(@Nullable Double yStep) {
        return (RangeXlyder) super.stepY(yStep);
    }

    @Override
    public RangeXlyder stepY(double yStep) {
        return (RangeXlyder) super.stepY(yStep);
    }

    @Override
    public RangeXlyder sliderFunction(SliderFunction sliderFunction) {
        return (RangeXlyder) super.sliderFunction(sliderFunction);
    }

    @Override
    public RangeXlyder sliderFunctionX(SliderFunction xSliderFunction) {
        return (RangeXlyder) super.sliderFunctionX(xSliderFunction);
    }

    @Override
    public RangeXlyder sliderFunctionY(SliderFunction ySliderFunction) {
        return (RangeXlyder) super.sliderFunctionY(ySliderFunction);
    }

    @Override
    public RangeXlyder incrementStep(@Nullable Double incrementStep) {
        return (RangeXlyder) super.incrementStep(incrementStep);
    }

    @Override
    public RangeXlyder incrementStep(double incrementStep) {
        return (RangeXlyder) super.incrementStep(incrementStep);
    }

    @Override
    public RangeXlyder incrementStepX(@Nullable Double xIncrementStep) {
        return (RangeXlyder) super.incrementStepX(xIncrementStep);
    }

    @Override
    public RangeXlyder incrementStepX(double xIncrementStep) {
        return (RangeXlyder) super.incrementStepX(xIncrementStep);
    }

    @Override
    public RangeXlyder incrementStepY(@Nullable Double yIncrementStep) {
        return (RangeXlyder) super.incrementStepY(yIncrementStep);
    }

    @Override
    public RangeXlyder incrementStepY(double yIncrementStep) {
        return (RangeXlyder) super.incrementStepY(yIncrementStep);
    }

    //endregion
}
