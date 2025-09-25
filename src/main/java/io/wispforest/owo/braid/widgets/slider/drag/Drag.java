package io.wispforest.owo.braid.widgets.slider.drag;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.slider.SliderCallback;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

public class Drag extends RawDrag {

    public Drag(
        double value,
        @Nullable WidgetSetupCallback<Drag> setupCallback,
        @Nullable SliderCallback onChanged
    ) {
        super(
            value,
            null,
            onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE)
        );
        if (setupCallback != null) setupCallback.setup(this);
    }

    public Drag(
        double value,
        @Nullable WidgetSetupCallback<Drag> setupCallback,
        SliderCallback onChanged,
        boolean active
    ) {
        this(value, setupCallback, active ? onChanged : null);
    }

    @Override
    public Drag min(@Nullable Double min) {
        return (Drag) super.min(min);
    }

    @Override
    public Drag min(double min) {
        return (Drag) super.min(min);
    }

    @Override
    public Drag max(@Nullable Double max) {
        return (Drag) super.max(max);
    }

    @Override
    public Drag max(double max) {
        return (Drag) super.max(max);
    }

    @Override
    public Drag range(@Nullable Double min, @Nullable Double max) {
        return (Drag) super.range(min, max);
    }

    @Override
    public Drag range(double min, double max) {
        return (Drag) super.range(min, max);
    }

    @Override
    public Drag step(@Nullable Double step) {
        return (Drag) super.step(step);
    }

    @Override
    public Drag step(double step) {
        return (Drag) super.step(step);
    }

    @Override
    public Drag dragFunction(DragFunction dragFunction) {
        return (Drag) super.dragFunction(dragFunction);
    }

    @Override
    public Drag axis(LayoutAxis axis) {
        return (Drag) super.axis(axis);
    }

    @Override
    public Drag vertical() {
        return (Drag) super.vertical();
    }

    @Override
    public Drag wrap(boolean wrap) {
        return (Drag) super.wrap(wrap);
    }

    @Override
    public Drag dragMultiplier(double dragMultiplier) {
        return (Drag) super.dragMultiplier(dragMultiplier);
    }

    @Override
    public Drag incrementStep(double incrementStep) {
        return (Drag) super.incrementStep(incrementStep);
    }
}
