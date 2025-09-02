package io.wispforest.owo.braid.widgets.slider.xlyder;

import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.braid.widgets.slider.slider.SliderFunction;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class Xlyder extends RawXlyder {

    public Xlyder(
        Vector2dc value,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        @Nullable XlyderCallback onChanged
    ) {
        super(
            value,
            null,
            onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            Size.square(8),
            new DefaultSliderHandle()
        );
        if (setupCallback != null) setupCallback.setup(this);
    }

    public Xlyder(
        Vector2dc value,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged
    ) {
        this(value, setupCallback, active ? onChanged : null);
    }

    public Xlyder(
        double x, double y,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        @Nullable XlyderCallback onChanged
    ) {
        this(new Vector2d(x, y), setupCallback, onChanged);
    }

    public Xlyder(
        double x, double y,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged
    ) {
        this(new Vector2d(x, y), setupCallback, active ? onChanged : null);
    }

    //region Setup Methods

    @Override
    public Xlyder min(Vector2d min) {
        return (Xlyder) super.min(min);
    }

    @Override
    public Xlyder min(double minX, double minY) {
        return (Xlyder) super.min(minX, minY);
    }

    @Override
    public Xlyder min(double min) {
        return (Xlyder) super.min(min);
    }

    @Override
    public Xlyder minX(double minX) {
        return (Xlyder) super.minX(minX);
    }

    @Override
    public Xlyder minY(double minY) {
        return (Xlyder) super.minY(minY);
    }

    @Override
    public Xlyder max(Vector2d max) {
        return (Xlyder) super.max(max);
    }

    @Override
    public Xlyder max(double maxX, double maxY) {
        return (Xlyder) super.max(maxX, maxY);
    }

    @Override
    public Xlyder max(double max) {
        return (Xlyder) super.max(max);
    }

    @Override
    public Xlyder maxX(double maxX) {
        return (Xlyder) super.maxX(maxX);
    }

    @Override
    public Xlyder maxY(double maxY) {
        return (Xlyder) super.maxY(maxY);
    }

    @Override
    public Xlyder range(Vector2d min, Vector2d max) {
        return (Xlyder) super.range(min, max);
    }

    @Override
    public Xlyder range(double minX, double minY, double maxX, double maxY) {
        return (Xlyder) super.range(minX, minY, maxX, maxY);
    }

    @Override
    public Xlyder rangeX(double minX, double maxX) {
        return (Xlyder) super.rangeX(minX, maxX);
    }

    @Override
    public Xlyder range(double min, double max) {
        return (Xlyder) super.range(min, max);
    }

    @Override
    public Xlyder rangeY(double minY, double maxY) {
        return (Xlyder) super.rangeY(minY, maxY);
    }

    @Override
    public Xlyder step(@Nullable Double step) {
        return (Xlyder) super.step(step);
    }

    @Override
    public Xlyder step(double step) {
        return (Xlyder) super.step(step);
    }

    @Override
    public Xlyder stepX(@Nullable Double xStep) {
        return (Xlyder) super.stepX(xStep);
    }

    @Override
    public Xlyder stepX(double xStep) {
        return (Xlyder) super.stepX(xStep);
    }

    @Override
    public Xlyder stepY(@Nullable Double yStep) {
        return (Xlyder) super.stepY(yStep);
    }

    @Override
    public Xlyder stepY(double yStep) {
        return (Xlyder) super.stepY(yStep);
    }

    @Override
    public Xlyder sliderFunction(SliderFunction sliderFunction) {
        return (Xlyder) super.sliderFunction(sliderFunction);
    }

    @Override
    public Xlyder sliderFunctionX(SliderFunction xSliderFunction) {
        return (Xlyder) super.sliderFunctionX(xSliderFunction);
    }

    @Override
    public Xlyder sliderFunctionY(SliderFunction ySliderFunction) {
        return (Xlyder) super.sliderFunctionY(ySliderFunction);
    }

    @Override
    public Xlyder incrementStep(@Nullable Double incrementStep) {
        return (Xlyder) super.incrementStep(incrementStep);
    }

    @Override
    public Xlyder incrementStep(double incrementStep) {
        return (Xlyder) super.incrementStep(incrementStep);
    }

    @Override
    public Xlyder incrementStepX(@Nullable Double xIncrementStep) {
        return (Xlyder) super.incrementStepX(xIncrementStep);
    }

    @Override
    public Xlyder incrementStepX(double xIncrementStep) {
        return (Xlyder) super.incrementStepX(xIncrementStep);
    }

    @Override
    public Xlyder incrementStepY(@Nullable Double yIncrementStep) {
        return (Xlyder) super.incrementStepY(yIncrementStep);
    }

    @Override
    public Xlyder incrementStepY(double yIncrementStep) {
        return (Xlyder) super.incrementStepY(yIncrementStep);
    }

    public Xlyder handleSize(Size handleSize) {
        this.assertMutable();
        this.handleSize = handleSize;
        return this;
    }

    public Size handleSize() {
        return this.handleSize;
    }

    //endregion
}
