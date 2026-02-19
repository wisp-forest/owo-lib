package io.wispforest.owo.braid.widgets.slider.drag;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.slider.slider.SliderCallback;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class RawDrag extends StatefulWidget {

    public final double value;
    protected @Nullable Double min = 0d;
    protected @Nullable Double max = 1d;
    protected @Nullable Double step;
    protected DragFunction dragFunction = DragFunction.LINEAR;
    protected double dragMultiplier = 1;
    protected LayoutAxis axis = LayoutAxis.HORIZONTAL;
    protected boolean wrap = false;

    public final @Nullable SliderCallback onChanged;
    public final @Nullable Widget child;

    protected @Nullable Double incrementStep = null;

    public RawDrag(
        double value,
        @Nullable WidgetSetupCallback<RawDrag> setupCallback,
        @Nullable SliderCallback onChanged,
        @Nullable Widget child
    ) {
        this.value = value;
        this.onChanged = onChanged;
        this.child = child;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public RawDrag min(@Nullable Double min) {
        this.assertMutable();
        this.min = min;
        return this;
    }

    public RawDrag min(double min) {
        this.assertMutable();
        this.min = min;
        return this;
    }

    public @Nullable Double min() {
        return this.min;
    }

    public RawDrag max(@Nullable Double max) {
        this.assertMutable();
        this.max = max;
        return this;
    }

    public RawDrag max(double max) {
        this.assertMutable();
        this.max = max;
        return this;
    }

    public @Nullable Double max() {
        return this.max;
    }

    public RawDrag range(@Nullable Double min, @Nullable Double max) {
        this.assertMutable();
        this.min = min;
        this.max = max;
        return this;
    }

    public RawDrag range(double min, double max) {
        this.assertMutable();
        this.min = min;
        this.max = max;
        return this;
    }

    public RawDrag step(@Nullable Double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public RawDrag step(double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public @Nullable Double step() {
        return this.step;
    }

    public RawDrag dragFunction(DragFunction dragFunction) {
        this.assertMutable();
        this.dragFunction = dragFunction;
        return this;
    }

    public DragFunction dragFunction() {
        return this.dragFunction;
    }

    public RawDrag axis(LayoutAxis axis) {
        this.assertMutable();
        this.axis = axis;
        return this;
    }

    public RawDrag vertical() {
        return this.axis(LayoutAxis.VERTICAL);
    }

    public LayoutAxis axis() {
        return this.axis;
    }

    public RawDrag wrap(boolean wrap) {
        this.assertMutable();
        this.wrap = wrap;
        return this;
    }

    public boolean wrap() {
        return this.wrap;
    }

    public RawDrag dragMultiplier(double dragMultiplier) {
        this.assertMutable();
        this.dragMultiplier = dragMultiplier;
        return this;
    }

    public double dragMultiplier() {
        return this.dragMultiplier;
    }

    public RawDrag incrementStep(double incrementStep) {
        this.assertMutable();
        this.incrementStep = incrementStep;
        return this;
    }

    public @Nullable Double incrementStep() {
        return this.incrementStep;
    }


    @Override
    public WidgetState<RawDrag> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawDrag> {

        protected double dragValue = 0;
        protected boolean dragging = false;

        protected double normalizedValue;
        protected double incrementStep;
        protected CursorStyle draggingCursorStyle = null;

        @Override
        public void init() {
            var widget = this.widget();
            // incrementStep in drag: when bounded, treat increment as fraction of range; when unbounded, as raw value units
            if (widget.min != null && widget.max != null) {
                var range = widget.max - widget.min;
                var inc = widget.incrementStep != null ? widget.incrementStep : (widget.step != null ? widget.step : range * 0.01);
                this.incrementStep = inc / (range == 0 ? 1 : range);
            } else {
                this.incrementStep = widget.incrementStep != null ? widget.incrementStep : (widget.step != null ? widget.step : 1.0);
            }
        }

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            this.normalizedValue = (widget.min != null && widget.max != null)? (widget.value - widget.min) / (widget.max - widget.min) : 0;
            this.draggingCursorStyle = null;
            return new LayoutBuilder((innerContext, constraints) -> {
                var size = constraints.maxFiniteOrMinSize();
                var content = new Sized(size, widget.child);
                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Incrementor(
                            widget.axis,
                            increment -> this.increment(constraints, increment),
                            new MouseArea(
                                mouseArea -> mouseArea
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;
                                        this.dragValue = this.normalizedValue;
                                        this.dragging = true;
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> {
                                        var delta = widget.axis.choose(dx, widget.axis == LayoutAxis.VERTICAL ? -dy : dy);
                                        this.move(constraints, delta);
                                    })
                                    .dragEndCallback(() -> dragging = false)
                                    .cursorStyleSupplier((x, y) -> {
                                        if (dragging) {
                                            if (draggingCursorStyle == null) this.draggingCursorStyle = CursorStyle.forDraggingAlong(widget.axis, context.instance().computeGlobalTransform());
                                            return this.draggingCursorStyle;
                                        }
                                        return CursorStyle.HAND;
                                    }),
                                content
                            )
                        )
                );
            });
        }

        protected void move(Constraints constraints, double deltaAlongAxis) {
            var widget = this.widget();
            if (widget.min != null && widget.max != null) {
                var track = Math.max(1, constraints.maxFiniteOrMinOnAxis(widget.axis));
                var cursorNorm = (deltaAlongAxis / track) * widget.dragMultiplier;
                var valueDelta = widget.dragFunction.deltaValue(widget.value, widget.min, widget.max, cursorNorm);
                var newValue = widget.value + valueDelta;
                this.applyValueBounded(newValue);
            } else {
                var track = Math.max(1, constraints.maxFiniteOrMinOnAxis(widget.axis));
                var cursorNorm = (deltaAlongAxis / track) * widget.dragMultiplier;
                var valueDelta = widget.dragFunction.deltaValue(widget.value, null, null, cursorNorm);
                this.applyValueUnbounded(widget.value + valueDelta);
            }
        }

        protected void increment(Constraints constraints, double increment) {
            var widget = this.widget();
            if (widget.min != null && widget.max != null) {
                var range = widget.max - widget.min;
                var valueDelta = incrementStep * increment * range;
                this.applyValueBounded(widget.value + valueDelta);
            } else {
                var unit = widget.incrementStep != null ? widget.incrementStep : (widget.step != null ? widget.step : 1.0);
                this.applyValueUnbounded(widget.value + unit * increment);
            }
        }

        protected void applyValueBounded(double newValue) {
            var widget = this.widget();
            var min = widget.min == null ? Double.NEGATIVE_INFINITY : widget.min;
            var max = widget.max == null ? Double.POSITIVE_INFINITY : widget.max;
            if (widget.wrap && widget.min != null && widget.max != null) {
                var range = max - min;
                if (range != 0) {
                    var offset = (newValue - min) % range;
                    if (offset < 0) offset += range;
                    newValue = min + offset;
                }
            } else {
                newValue = Mth.clamp(newValue, min, max);
            }
            var step = widget.step;
            newValue = step != null ? Math.round(newValue / step) * step : newValue;
            if (widget.wrap && widget.min != null && widget.max != null) {
                var range = max - min;
                if (range != 0) {
                    var offset = (newValue - min) % range;
                    if (offset < 0) offset += range;
                    newValue = min + offset;
                }
            } else {
                newValue = Mth.clamp(newValue, min, max);
            }
            widget.onChanged.accept(newValue);
        }

        protected void applyValueUnbounded(double newValue) {
            var widget = this.widget();
            var step = widget.step;
            widget.onChanged.accept(step != null ? Math.round(newValue / step) * step : newValue);
        }
    }

}
