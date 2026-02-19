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
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.slider.SliderStyle;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Slider extends StatefulWidget {

    public final double value;
    public final @Nullable SliderCallback onChanged;

    protected double min = 0;
    protected double max = 1;
    protected @Nullable Double step;
    protected SliderFunction function = SliderFunction.LINEAR;
    protected LayoutAxis axis = LayoutAxis.HORIZONTAL;
    protected @Nullable Double incrementStep = null;
    protected @Nullable SliderStyle<Double> style;

    public Slider(
        double value,
        @Nullable WidgetSetupCallback<Slider> setupCallback,
        @Nullable SliderCallback onChanged
    ) {
        this.value = value;
        this.onChanged = onChanged;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public Slider(
        double value,
        @Nullable WidgetSetupCallback<Slider> setupCallback,
        boolean active,
        SliderCallback onChanged
    ) {
        this(value, setupCallback, active ? onChanged : null);
    }

    public Slider min(double min) {
        this.assertMutable();
        this.min = min;
        return this;
    }

    public double min() {
        return this.min;
    }

    public Slider max(double max) {
        this.assertMutable();
        this.max = max;
        return this;
    }

    public double max() {
        return this.max;
    }

    public Slider range(double min, double max) {
        this.assertMutable();
        this.min = min;
        this.max = max;
        return this;
    }

    public Slider step(@Nullable Double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public Slider step(double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public @Nullable Double step() {
        return this.step;
    }

    public Slider function(SliderFunction sliderFunction) {
        this.assertMutable();
        this.function = sliderFunction;
        return this;
    }

    public SliderFunction function() {
        return this.function;
    }

    public Slider axis(LayoutAxis axis) {
        this.assertMutable();
        this.axis = axis;
        return this;
    }

    public Slider vertical() {
        return this.axis(LayoutAxis.VERTICAL);
    }

    public LayoutAxis axis() {
        return this.axis;
    }

    public Slider incrementStep(double incrementStep) {
        this.assertMutable();
        this.incrementStep = incrementStep;
        return this;
    }

    public @Nullable Double incrementStep() {
        return this.incrementStep;
    }

    public Slider style(SliderStyle<Double> style) {
        this.assertMutable();
        this.style = style;
        return this;
    }

    public @Nullable SliderStyle<Double> style() {
        return this.style;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<Slider> {

        protected double dragValue = 0;
        protected boolean dragging = false;

        protected double normalizedValue;
        protected double incrementStep;
        protected CursorStyle draggingCursorStyle = null;

        protected double handleSize;

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            var effectiveStyle = widget.style != null ? widget.style : SliderStyle.<Double>getDefault();
            if (DefaultSliderStyle.maybeOf(context) instanceof SliderStyle<Double> contextStyle) {
                effectiveStyle = effectiveStyle.overriding(contextStyle);
            }

            var disabled = widget.onChanged == null || ControlsOverride.controlsDisabled(context);

            var track = Objects.requireNonNullElse(effectiveStyle.track(), DEFAULT_TRACK);
            var handle = Objects.requireNonNullElse(effectiveStyle.handleBuilder(), DEFAULT_HANDLE_BUILDER).build(!disabled);
            this.handleSize = Objects.requireNonNullElse(effectiveStyle.handleSize(), DEFAULT_HANDLE_SIZE);
            //noinspection OptionalAssignedToNull
            var confirmSound = effectiveStyle.confirmSound() != null ? effectiveStyle.confirmSound().orElse(null) : SoundEvents.UI_BUTTON_CLICK.value();

            this.normalizedValue = widget.function.normalize(widget.value, widget.min, widget.max);
            var trueMin = Math.min(widget.max, widget.min);
            var trueMax = Math.max(widget.max, widget.min);
            this.incrementStep = widget.incrementStep != null
                ? widget.function.normalize(widget.incrementStep, trueMin, trueMax)
                : widget.step != null
                    ? widget.function.normalize(widget.step, trueMin, trueMax)
                    : 0.01;
            this.draggingCursorStyle = null;

            return new LayoutBuilder((innerContext, constraints) -> {
                var size = constraints.maxFiniteOrMinSize();
                var content = new Stack(
                    widget.axis.choose(Alignment.LEFT, Alignment.TOP),
                    new Sized(size, track),
                    new Padding(
                        widget.axis.chooseCompute(
                            () -> Insets.left(Math.floor((size.width() - this.handleSize) * this.normalizedValue)),
                            () -> Insets.top(Math.floor((size.height() - this.handleSize) * (1 - this.normalizedValue)))
                        ),
                        widget.axis.chooseCompute(
                            () -> new Sized(this.handleSize, size.height(), handle),
                            () -> new Sized(size.width(), this.handleSize, handle)
                        )
                    )
                );
                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Incrementor(
                            widget.axis,
                            increment -> this.applyValue(Mth.clamp(this.normalizedValue + this.incrementStep * increment, 0, 1)),
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
                                    .dragEndCallback(() -> {
                                        this.dragging = false;
                                        if (confirmSound != null) {
                                            UISounds.play(confirmSound);
                                        }
                                    })
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

            var trackLength = constraints.maxFiniteOrMinOnAxis(axis) - this.handleSize;
            var handleMin = this.normalizedValue * trackLength;
            var handleMax = handleMin + this.handleSize;

            var coordinate = axis.choose(x, y);
            return coordinate >= handleMin && coordinate <= handleMax;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            this.dragValue += this.widget().axis.choose(dx, dy) / (constraints.maxFiniteOrMinOnAxis(this.widget().axis) - this.handleSize);

            this.applyValue(Mth.clamp(this.dragValue, 0, 1));
        }

        protected double setAbsolute(Constraints constraints, double x, double y) {
            if (this.widget().onChanged == null) return this.normalizedValue;

            var axis = this.widget().axis;
            var newNormalizedValue = Mth.clamp((axis.choose(x, y) - this.handleSize / 2) / (constraints.maxFiniteOrMinOnAxis(axis) - this.handleSize), 0, 1);

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

    // ---

    private static final Widget DEFAULT_TRACK = new Panel(ButtonComponent.DISABLED_TEXTURE);
    private static final SliderStyle.HandleBuilder DEFAULT_HANDLE_BUILDER = DefaultSliderHandle::new;
    private static final double DEFAULT_HANDLE_SIZE = 8.0;
}
