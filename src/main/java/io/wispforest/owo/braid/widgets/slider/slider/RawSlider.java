package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class RawSlider extends StatefulWidget {

    public final double value;
    protected double min = 0;
    protected double max = 1;
    protected @Nullable Double step;
    protected SliderFunction function = SliderFunction.LINEAR;
    protected LayoutAxis axis = LayoutAxis.HORIZONTAL;
    protected double handleSize;

    public final @Nullable SliderCallback onChanged;
    public final @Nullable Widget track;
    public final Widget handle;

    protected @Nullable Double incrementStep = null;

    public RawSlider(
        double value,
        @Nullable WidgetSetupCallback<RawSlider> setupCallback,
        @Nullable SliderCallback onChanged,
        @Nullable Widget track,
        double handleSize,
        Widget handle
    ) {
        this.value = value;
        this.onChanged = onChanged;
        this.track = track;
        this.handleSize = handleSize;
        this.handle = handle;
        if (setupCallback != null) setupCallback.setup(this);
    }

    //region Setup Methods

    public RawSlider min(double min) {
        this.assertMutable();
        this.min = min;
        return this;
    }

    public double min() {
        return this.min;
    }

    public RawSlider max(double max) {
        this.assertMutable();
        this.max = max;
        return this;
    }

    public double max() {
        return this.max;
    }

    public RawSlider range(double min, double max) {
        this.assertMutable();
        this.min = min;
        this.max = max;
        return this;
    }

    public RawSlider step(@Nullable Double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public RawSlider step(double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public @Nullable Double step() {
        return this.step;
    }

    public RawSlider function(SliderFunction sliderFunction) {
        this.assertMutable();
        this.function = sliderFunction;
        return this;
    }

    public SliderFunction function() {
        return this.function;
    }

    public RawSlider axis(LayoutAxis axis) {
        this.assertMutable();
        this.axis = axis;
        return this;
    }

    public RawSlider vertical() {
        return this.axis(LayoutAxis.VERTICAL);
    }

    public LayoutAxis axis() {
        return this.axis;
    }

    public RawSlider incrementStep(double incrementStep) {
        this.assertMutable();
        this.incrementStep = incrementStep;
        return this;
    }

    public @Nullable Double incrementStep() {
        return this.incrementStep;
    }

    //endregion

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawSlider> {

        protected double dragValue = 0;
        protected boolean dragging = false;

        protected double normalizedValue;
        protected double incrementStep;
        protected CursorStyle draggingCursorStyle = null;

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            this.normalizedValue = widget.function.normalize(widget.value, widget.min, widget.max);
            this.incrementStep = widget.incrementStep != null
                ? widget.function.normalize(widget.incrementStep, widget.min, widget.max)
                : widget.step != null
                    ? widget.function.normalize(widget.step, widget.min, widget.max)
                    : 0.01;
            this.draggingCursorStyle = null;
            return new LayoutBuilder((innerContext, constraints) -> {
                var size = constraints.maxFiniteOrMinSize();
                var content = new Stack(
                    widget.axis.choose(Alignment.LEFT, Alignment.TOP),
                    new Sized(size, widget.track),
                    new Padding(
                        widget.axis.chooseCompute(
                            () -> Insets.left(Math.floor((size.width() - widget.handleSize) * this.normalizedValue)),
                            () -> Insets.top(Math.floor((size.height() - widget.handleSize) * (1 - this.normalizedValue)))
                        ),
                        widget.axis.chooseCompute(
                            () -> new Sized(widget.handleSize, size.height(), widget.handle),
                            () -> new Sized(size.width(), widget.handleSize, widget.handle)
                        )
                    )
                );
                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Incrementor(
                            widget.axis,
                            increment -> this.applyValue(MathHelper.clamp(this.normalizedValue + this.incrementStep * increment, 0, 1)),
                            new MouseArea(
                                mouseArea -> mouseArea
                                    //TODO: decide what to do with buttons here
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;

                                        if (widget.axis == LayoutAxis.VERTICAL) y = constraints.maxFiniteOrMinOnAxis(widget.axis) - y;
                                        var initialDragValue = this.normalizedValue;
                                        if (!this.isInHandle(constraints, x, y)) initialDragValue = this.setAbsolute(constraints, x, y);

                                        this.dragValue = initialDragValue;
                                        this.dragging = true;
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> this.move(constraints, dx, widget.axis == LayoutAxis.VERTICAL ? -dy : dy))
                                    .dragEndCallback(() -> this.dragging = false)
                                    .cursorStyleSupplier((x, y) -> {
                                        //TODO: invert the y passed in here cuz its cringe atm
                                        if (!this.isInHandle(constraints, x, constraints.maxHeight() - y) && !this.dragging) return CursorStyle.HAND;
                                        if (this.draggingCursorStyle == null) this.draggingCursorStyle = CursorStyle.forDraggingAlong(widget.axis, context.instance().computeGlobalTransform());
                                        return this.draggingCursorStyle;
                                    }),
                                content
                            )
                        )
                );
            });
        }

        protected boolean isInHandle(Constraints constraints, double x, double y) {
            var axis = this.widget().axis;

            var trackLength = constraints.maxFiniteOrMinOnAxis(axis) - this.widget().handleSize;
            var handleMin = this.normalizedValue * trackLength;
            var handleMax = handleMin + this.widget().handleSize;

            var coordinate = axis.choose(x, y);
            return coordinate >= handleMin && coordinate <= handleMax;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            this.dragValue += this.widget().axis.choose(dx, dy) / (constraints.maxFiniteOrMinOnAxis(this.widget().axis) - this.widget().handleSize);

            this.applyValue(MathHelper.clamp(this.dragValue, 0, 1));
        }

        protected double setAbsolute(Constraints constraints, double x, double y) {
            if (this.widget().onChanged == null) return this.normalizedValue;

            var axis = this.widget().axis;
            var handleSize = this.widget().handleSize;

            var newNormalizedValue = MathHelper.clamp((axis.choose(x, y) - handleSize / 2) / (constraints.maxFiniteOrMinOnAxis(axis) - handleSize), 0, 1);

            this.applyValue(newNormalizedValue);
            return newNormalizedValue;
        }

        protected void applyValue(double newNormalizedValue) {
            var widget = this.widget();
            var step = widget.step;
            var newValue = widget.function.deNormalize(newNormalizedValue, widget.min, widget.max);
            this.widget().onChanged.accept(step != null ? Math.round(newValue / step) * step : newValue);
        }
    }
}
