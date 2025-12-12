package io.wispforest.owo.braid.widgets.slider.range;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.slider.slider.SliderFunction;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RangeSlider extends StatefulWidget {

    protected double min = 0;
    protected double max = 1;
    protected double minRange = 0;
    protected double maxRange = -1;
    protected @Nullable Double step;
    protected SliderFunction sliderFunction = SliderFunction.LINEAR;
    protected LayoutAxis axis = LayoutAxis.HORIZONTAL;
    protected @Nullable Double incrementStep = null;
    protected @Nullable RangeSliderStyle style = null;

    public final double minValue, maxValue;
    public final @Nullable RangeSliderCallback onChanged;

    public RangeSlider(
        double minValue,
        double maxValue,
        @Nullable WidgetSetupCallback<RangeSlider> setupCallback,
        @Nullable RangeSliderCallback onChanged
    ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.onChanged = onChanged;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public RangeSlider(
        double minValue,
        double maxValue,
        @Nullable WidgetSetupCallback<RangeSlider> setupCallback,
        boolean active,
        RangeSliderCallback onChanged
    ) {
        this(
            minValue, maxValue,
            setupCallback,
            active ? onChanged : null
        );
    }

    public RangeSlider min(double min) {
        this.assertMutable();
        this.min = min;
        return this;
    }

    public double min() {
        return this.min;
    }

    public RangeSlider max(double max) {
        this.assertMutable();
        this.max = max;
        return this;
    }

    public double max() {
        return this.max;
    }

    public RangeSlider range(double min, double max) {
        this.assertMutable();
        this.min = min;
        this.max = max;
        return this;
    }

    public RangeSlider minRange(double minRange) {
        this.assertMutable();
        this.minRange = minRange;
        return this;
    }

    public double minRange() {
        return this.minRange;
    }

    public RangeSlider maxRange(double maxRange) {
        this.assertMutable();
        this.maxRange = maxRange;
        return this;
    }

    public double maxRange() {
        return this.maxRange;
    }

    public RangeSlider clampRange(double minRange, double maxRange) {
        this.assertMutable();
        this.minRange = minRange;
        this.maxRange = maxRange;
        return this;
    }

    public RangeSlider step(@Nullable Double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public RangeSlider step(double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public @Nullable Double step() {
        return this.step;
    }

    public RangeSlider sliderFunction(SliderFunction function) {
        this.assertMutable();
        this.sliderFunction = function;
        return this;
    }

    public SliderFunction sliderFunction() {
        return this.sliderFunction;
    }

    public RangeSlider axis(LayoutAxis axis) {
        this.assertMutable();
        this.axis = axis;
        return this;
    }

    public RangeSlider vertical() {
        return this.axis(LayoutAxis.VERTICAL);
    }

    public LayoutAxis axis() {
        return this.axis;
    }

    public RangeSlider incrementStep(double incrementStep) {
        this.assertMutable();
        this.incrementStep = incrementStep;
        return this;
    }

    public @Nullable Double incrementStep() {
        return this.incrementStep;
    }

    public RangeSlider style(RangeSliderStyle style) {
        this.assertMutable();
        this.style = style;
        return this;
    }

    public @Nullable RangeSliderStyle style() {
        return this.style;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<RangeSlider> {

        protected double dragValue = 0;
        protected @Nullable Handle grabbedHandle = null;
        protected boolean dragging = false;

        protected double normalizedMin;
        protected double normalizedMax;
        protected double incrementStep;
        protected CursorStyle draggingCursorStyle = null;
        protected double dragWidth;

        protected double minHandleSize;
        protected double maxHandleSize;

        @Override
        public void init() {
            var widget = this.widget();
            var trueMin = Math.min(widget.min, widget.max);
            var trueMax = Math.max(widget.min, widget.max);
            this.incrementStep = widget.incrementStep != null
                ? widget.sliderFunction.normalize(widget.incrementStep, trueMin, trueMax)
                : widget.step != null
                    ? widget.sliderFunction.normalize(widget.step, trueMin, trueMax)
                    : 0.01;
        }

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            var effectiveStyle = widget.style != null ? widget.style : RangeSliderStyle.DEFAULT;
            if (DefaultRangeSliderStyle.maybeOf(context) instanceof RangeSliderStyle contextStyle) {
                effectiveStyle = effectiveStyle.overriding(contextStyle);
            }

            var disabled = widget.onChanged == null || ControlsOverride.controlsDisabled(context);

            var track = Objects.requireNonNullElse(effectiveStyle.track(), DEFAULT_TRACK);
            var rangeIndicator = Objects.requireNonNullElse(effectiveStyle.rangeIndicator(), DEFAULT_RANGE_INDICATOR);
            var minHandle = Objects.requireNonNullElse(effectiveStyle.minHandleBuilder(), DEFAULT_HANDLE_BUILDER).build(!disabled);
            var maxHandle = Objects.requireNonNullElse(effectiveStyle.maxHandleBuilder(), DEFAULT_HANDLE_BUILDER).build(!disabled);
            this.minHandleSize = Objects.requireNonNullElse(effectiveStyle.minHandleSize(), DEFAULT_HANDLE_SIZE);
            this.maxHandleSize = Objects.requireNonNullElse(effectiveStyle.maxHandleSize(), DEFAULT_HANDLE_SIZE);
            //noinspection OptionalAssignedToNull
            var confirmSound = effectiveStyle.confirmSound() != null ? effectiveStyle.confirmSound().orElse(null) : SoundEvents.UI_BUTTON_CLICK.value();

            this.normalizedMin = widget.sliderFunction.normalize(widget.minValue, widget.min, widget.max);
            this.normalizedMax = widget.sliderFunction.normalize(widget.maxValue, widget.min, widget.max);
            this.draggingCursorStyle = null;

            return new LayoutBuilder((innerContext, constraints) -> {
                var combinedHandleSize = this.minHandleSize + this.maxHandleSize;
                var rangeExtent = Math.ceil((constraints.maxOnAxis(widget.axis) - combinedHandleSize) * (this.normalizedMax - this.normalizedMin));

                var content = new Stack(
                    widget.axis.choose(Alignment.LEFT, Alignment.TOP),
                    new Sized(constraints.maxWidth(), constraints.maxHeight(), track),
                    new Padding(
                        widget.axis.chooseCompute(
                            () -> Insets.left(this.minHandleSize + Math.floor((constraints.maxWidth() - this.minHandleSize * 2) * this.normalizedMin) - 1),
                            () -> Insets.top(this.minHandleSize + Math.floor((constraints.maxHeight() - this.minHandleSize * 2) * this.normalizedMin) - 1)
                        ),
                        new Center(
                            1.0, null,
                            widget.axis.chooseCompute(
                                () -> new Sized(rangeExtent + 2, constraints.maxHeight(), rangeIndicator),
                                () -> new Sized(constraints.maxWidth(), rangeExtent + 2, rangeIndicator)
                            )
                        )
                    ),
                    new Padding(
                        widget.axis.chooseCompute(
                            () -> Insets.left(Math.floor((constraints.maxWidth() - this.minHandleSize * 2) * this.normalizedMin)),
                            () -> Insets.top(Math.floor((constraints.maxHeight() - this.minHandleSize * 2) * this.normalizedMin))
                        ),
                        widget.axis.chooseCompute(
                            () -> new Sized(this.minHandleSize, constraints.maxHeight(), minHandle),
                            () -> new Sized(constraints.maxWidth(), this.minHandleSize, minHandle)
                        )
                    ),
                    new Padding(
                        widget.axis.chooseCompute(
                            () -> Insets.left(this.maxHandleSize + Math.floor((constraints.maxWidth() - this.maxHandleSize * 2) * this.normalizedMax)),
                            () -> Insets.top(this.maxHandleSize + Math.floor((constraints.maxHeight() - this.maxHandleSize * 2) * this.normalizedMax))
                        ),
                        widget.axis.chooseCompute(
                            () -> new Sized(this.maxHandleSize, constraints.maxHeight(), maxHandle),
                            () -> new Sized(constraints.maxWidth(), this.maxHandleSize, maxHandle)
                        )
                    )
                );

                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Incrementor(
                            widget.axis,
                            this::increment,
                            new MouseArea(
                                mousearea -> mousearea
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;

                                        if (widget.axis == LayoutAxis.VERTICAL) y = constraints.maxFiniteOrMinOnAxis(widget.axis) - y;
                                        this.grabbedHandle = this.handleAt(constraints, x, y);
                                        if (this.grabbedHandle == Handle.BOTH && !this.isInRange(constraints, x, y)) {
                                            this.grabbedHandle = this.nearestHandle(constraints, x, y);
                                        }
                                        var initialDragValue = this.grabbedHandle == Handle.MAX ? this.normalizedMax : this.normalizedMin;

                                        if (!this.isInHandle(constraints, x, y) && this.grabbedHandle != Handle.BOTH) {
                                            initialDragValue = this.setAbsolute(constraints, x, y);
                                        }

                                        this.dragWidth = this.normalizedMax - this.normalizedMin;
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
                                        if (!isInHandle(constraints, x, constraints.maxHeight() - y) && !isInRange(constraints, x, constraints.maxHeight() - y) && !dragging) return CursorStyle.HAND;
                                        if (this.isInRange(constraints, x, y)) return CursorStyle.MOVE;
                                        if (this.draggingCursorStyle == null) this.draggingCursorStyle = CursorStyle.forDraggingAlong(widget.axis, context.instance().computeGlobalTransform());
                                        return this.draggingCursorStyle;
                                    }),
                                content
                            )
                        )
                );
            });
        }

        protected Handle handleAt(Constraints constraints, double x, double y) {
            var widget = this.widget();
            if (widget.axis == LayoutAxis.VERTICAL) y = constraints.maxFiniteOrMinOnAxis(widget.axis) - y;
            var coordinate = widget.axis.choose(x, y);
            var minStart = Math.floor((constraints.maxFiniteOrMinOnAxis(widget.axis) - this.minHandleSize * 2) * this.normalizedMin);
            var maxStart = this.maxHandleSize + Math.floor((constraints.maxFiniteOrMinOnAxis(widget.axis) - this.maxHandleSize * 2) * this.normalizedMax);

            var inMin = coordinate >= minStart && coordinate <= minStart + this.minHandleSize;
            var inMax = coordinate >= maxStart && coordinate <= maxStart + this.maxHandleSize;

            if (inMin && inMax) return Handle.BOTH;
            if (inMin) return Handle.MIN;
            if (inMax) return Handle.MAX;
            if (coordinate > minStart + this.minHandleSize && coordinate < maxStart) return Handle.BOTH;
            var distToMin = Math.abs(coordinate - (minStart + this.minHandleSize / 2));
            var distToMax = Math.abs(coordinate - (maxStart + this.maxHandleSize / 2));
            return distToMin <= distToMax ? Handle.MIN : Handle.MAX;
        }

        protected boolean isInHandle(Constraints constraints, double x, double y) {
            var widget = this.widget();
            if (widget.axis == LayoutAxis.VERTICAL) y = constraints.maxFiniteOrMinOnAxis(widget.axis) - y;
            var coordinate = widget.axis.choose(x, y);
            var minStart = Math.floor((constraints.maxFiniteOrMinOnAxis(widget.axis) - this.minHandleSize * 2) * this.normalizedMin);
            var maxStart = this.maxHandleSize + Math.floor((constraints.maxFiniteOrMinOnAxis(widget.axis) - this.maxHandleSize * 2) * this.normalizedMax);
            return (coordinate >= minStart && coordinate <= minStart + this.minHandleSize) || (coordinate >= maxStart && coordinate <= maxStart + this.maxHandleSize);
        }

        protected boolean isInRange(Constraints constraints, double x, double y) {
            var widget = this.widget();
            if (widget.axis == LayoutAxis.VERTICAL) y = constraints.maxFiniteOrMinOnAxis(widget.axis) - y;
            var coordinate = widget.axis.choose(x, y);
            var minEnd = Math.floor((constraints.maxFiniteOrMinOnAxis(widget.axis) - this.minHandleSize * 2) * this.normalizedMin) + this.minHandleSize;
            var maxStart = this.maxHandleSize + Math.floor((constraints.maxFiniteOrMinOnAxis(widget.axis) - this.maxHandleSize * 2) * this.normalizedMax);
            return coordinate >= minEnd && coordinate <= maxStart;
        }

        protected Handle nearestHandle(Constraints constraints, double x, double y) {
            var widget = this.widget();
            var trackLength = constraints.maxOnAxis(widget.axis) - (this.minHandleSize + this.maxHandleSize);
            var minCenter = this.normalizedMin * trackLength + this.minHandleSize / 2;
            var maxCenter = this.normalizedMax * trackLength + this.maxHandleSize / 2;
            var coordinate = widget.axis.choose(x, y);
            return Math.abs(coordinate - minCenter) <= Math.abs(coordinate - maxCenter) ? Handle.MIN : Handle.MAX;
        }

        protected double normalizedValueAt(Constraints constraints, double x, double y, @Nullable Handle grabbedHandle) {
            var widget = this.widget();
            if (widget.axis == LayoutAxis.VERTICAL) y = constraints.maxFiniteOrMinOnAxis(widget.axis) - y;
            double coordinate = widget.axis.choose(x, y);

            if (grabbedHandle == Handle.MAX) {
                var denom = Math.max(1, constraints.maxFiniteOrMinOnAxis(widget.axis) - this.maxHandleSize * 2);
                return Mth.clamp((coordinate - this.maxHandleSize * 1.5) / denom, 0, 1);
            } else {
                var denom = Math.max(1, constraints.maxFiniteOrMinOnAxis(widget.axis) - this.minHandleSize * 2);
                return Mth.clamp((coordinate - this.minHandleSize / 2) / denom, 0, 1);
            }
        }

        protected double setAbsolute(Constraints constraints, double x, double y) {
            if (this.widget().onChanged == null) return this.grabbedHandle == Handle.MAX ? this.normalizedMax : this.normalizedMin;
            var normalizedValue = this.normalizedValueAt(constraints, x, y, this.grabbedHandle);
            var newNormalizedMin = this.normalizedMin;
            var newNormalizedMax = this.normalizedMax;
            var minRangeNorm = this.minRangeNorm();
            var maxRangeNorm = this.maxRangeNorm();

            if (this.grabbedHandle == Handle.MIN) {
                var upper = newNormalizedMax - minRangeNorm;
                var lower = maxRangeNorm >= 0 ? newNormalizedMax - maxRangeNorm : 0;
                newNormalizedMin = Mth.clamp(normalizedValue, Math.max(0, lower), Math.max(0, upper));
            } else if (this.grabbedHandle == Handle.MAX) {
                var lower = newNormalizedMin + minRangeNorm;
                var upper = maxRangeNorm >= 0 ? newNormalizedMin + maxRangeNorm : 1;
                newNormalizedMax = Mth.clamp(normalizedValue, Math.min(1, lower), Math.min(1, upper));
            }

            this.applyValue(newNormalizedMin, newNormalizedMax);

            return this.grabbedHandle == Handle.MAX ? newNormalizedMax : newNormalizedMin;
        }


        protected void move(Constraints constraints, double dx, double dy) {
            if (this.widget().onChanged == null || this.grabbedHandle == null) return;
            var axis = this.widget().axis;
            var combinedHandleSize = this.minHandleSize + this.maxHandleSize;
            var track = constraints.maxFiniteOrMinOnAxis(axis) - combinedHandleSize;

            var deltaNorm = (axis.choose(dx, dy)) / track;
            this.dragValue += deltaNorm;
            var minRangeNorm = this.minRangeNorm();
            var maxRangeNorm = this.maxRangeNorm();

            switch (this.grabbedHandle) {
                case MIN -> {
                    var maxCap = this.normalizedMax - minRangeNorm;
                    var minCap = maxRangeNorm >= 0 ? this.normalizedMax - maxRangeNorm : 0;
                    var newMin = Mth.clamp(this.dragValue, Math.max(0, minCap), Math.max(0, maxCap));
                    this.applyValue(newMin, this.normalizedMax);
                }
                case MAX -> {
                    var minCap = this.normalizedMin + minRangeNorm;
                    var maxCap = maxRangeNorm >= 0 ? this.normalizedMin + maxRangeNorm : 1;
                    var newMax = Mth.clamp(this.dragValue, Math.min(1, minCap), Math.min(1, maxCap));
                    this.applyValue(this.normalizedMin, newMax);
                }
                case BOTH -> {
                    var width = this.dragWidth;
                    this.dragValue = Mth.clamp(this.dragValue, 0, 1 - width);
                    var newMin = this.dragValue;
                    var newMax = newMin + width;
                    this.applyValue(newMin, newMax);
                }
            }
        }

        protected void increment(double increment) {
            if (this.widget().onChanged == null) return;
            var target = this.grabbedHandle != null ? this.grabbedHandle : Handle.BOTH;
            var delta = this.incrementStep * increment;
            var newMin = this.normalizedMin;
            var newMax = this.normalizedMax;
            var minRangeNorm = this.minRangeNorm();
            var maxRangeNorm = this.maxRangeNorm();
            if (target == Handle.MIN) {
                var upper = newMax - minRangeNorm;
                var lower = maxRangeNorm >= 0 ? newMax - maxRangeNorm : 0;
                newMin = Mth.clamp(newMin + delta, Math.max(0, lower), Math.max(0, upper));
            } else if (target == Handle.MAX) {
                var lower = newMin + minRangeNorm;
                var upper = maxRangeNorm >= 0 ? newMin + maxRangeNorm : 1;
                newMax = Mth.clamp(newMax + delta, Math.min(1, lower), Math.min(1, upper));
            } else {
                newMin = Mth.clamp(newMin + delta, 0, 1);
                newMax = Mth.clamp(newMax + delta, 0, 1);
            }
            this.applyValue(newMin, newMax);
        }

        protected void applyValue(double newNormalizedMin, double newNormalizedMax) {
            var widget = this.widget();

            var newMinValue = widget.sliderFunction.deNormalize(newNormalizedMin, widget.min, widget.max);
            var newMaxValue = widget.sliderFunction.deNormalize(newNormalizedMax, widget.min, widget.max);

            if (widget.step != null) {
                var step = widget.step;
                newMinValue = Math.round(newMinValue / step) * step;
                newMaxValue = Math.round(newMaxValue / step) * step;
            }

            newMinValue = Mth.clamp(newMinValue, widget.min, widget.max);
            newMaxValue = Mth.clamp(newMaxValue, widget.min, widget.max);

            widget.onChanged.accept(newMinValue, newMaxValue);
        }

        protected double minRangeNorm() {
            var widget = this.widget();
            if (widget.minRange <= 0) return 0;
            var a = widget.sliderFunction.normalize(widget.min, widget.min, widget.max);
            var b = widget.sliderFunction.normalize(widget.min + widget.minRange, widget.min, widget.max);
            return Math.max(0, b - a);
        }

        protected double maxRangeNorm() {
            var widget = this.widget();
            if (widget.maxRange < 0) return -1;
            var a = widget.sliderFunction.normalize(widget.min, widget.min, widget.max);
            var b = widget.sliderFunction.normalize(widget.min + widget.maxRange, widget.min, widget.max);
            return Math.max(0, b - a);
        }

        protected enum Handle {
            MIN, MAX, BOTH
        }
    }

    // ---

    private static final Widget DEFAULT_TRACK = new Panel(ButtonComponent.DISABLED_TEXTURE);
    private static final Widget DEFAULT_RANGE_INDICATOR = new Box(new Color(0x7f000000));
    private static final RangeSliderStyle.HandleBuilder DEFAULT_HANDLE_BUILDER = DefaultSliderHandle::new;
    private static final double DEFAULT_HANDLE_SIZE = 8.0;
}
