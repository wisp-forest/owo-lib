package io.wispforest.owo.braid.widgets.slider.drag;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.slider.SliderCallback;
import io.wispforest.owo.braid.widgets.slider.ValueMapper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

/// A low-level [Widget] that allows dragging on [#child] to change a value.<br>
///
///
public class RawDrag extends StatefulWidget {

    public final double value;
    public final double normalizedValue;
    private @Nullable Double min = 0d, max = 1d;
    private @Nullable Double step;
    private boolean wrap = false;
    private LayoutAxis axis = LayoutAxis.HORIZONTAL;
    private ValueMapper valueMapper = ValueMapper.LINEAR;
    private @Nullable SliderCallback onChanged;
    public final @Nullable Widget child;

    public RawDrag(
        double value,
        WidgetSetupCallback<RawDrag> setupCallback,
        @Nullable Widget child
    ) {
        this.value = value;
        this.child = child;
        setupCallback.setup(this);
        this.normalizedValue = normalizeValue(value, this.min, this.max, this.wrap);
    }

    /// Sets the minimum value for this Drag.<br>
    /// `null` will remove the minimum value.
    ///
    /// **Note:**  Providing a higher min than maximum can be used to invert the drag's direction
    ///
    /// @see #max(Double)
    /// @see #range(Double, Double)
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

    /// Sets the maximum value for this Drag.<br>
    /// `null` will remove the maximum value.
    ///
    /// **Note:**  Providing a lower maximum than minimum can be used to invert the drag's direction
    ///
    /// @see #min(Double)
    /// @see #range(Double, Double)
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

    /// Sets both the minimum and maximum values for this Drag.<br>
    /// `null` will remove the respective value.
    ///
    /// **Note:** Providing a lower maximum than minimum can be used to invert the drag's direction.
    ///
    /// @see #min(Double)
    /// @see #max(Double)
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

    /// Sets the step size of this Drag.<br>
    /// `null` will make it continuous
    public RawDrag step(@Nullable Double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    public @Nullable Double step() {
        return this.step;
    }

    /// Sets whether the drag's value wraps around when exceeding the min or max.
    public RawDrag wrap(boolean wrap) {
        this.assertMutable();
        this.wrap = wrap;
        return this;
    }

    public boolean wrap() {
        return this.wrap;
    }

    /// Sets the callback to be invoked when the value changes.<br>
    /// `null` will
    public RawDrag onChanged(@Nullable SliderCallback onChanged) {
        this.assertMutable();
        this.onChanged = onChanged;
        return this;
    }

    public @Nullable SliderCallback onChanged() {
        return this.onChanged;
    }


    public RawDrag axis(LayoutAxis axis) {
        this.assertMutable();
        this.axis = axis;
        return this;
    }

    /// @return The axis along which this drag is draggable.
    /// @see #axis(LayoutAxis)
    public LayoutAxis axis() {
        return this.axis;
    }

    @Override
    public WidgetState<RawDrag> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawDrag> {

        protected double dragValue = 0;

        @Override
        public Widget build(BuildContext context) {
            return new LayoutBuilder((innerContext, constraints) -> {
                var widget = this.widget();
                var size = constraints.maxFiniteOrMinSize();
                var content = new Sized(size, widget.child);
                var step = widget.step != null ? widget.step
                    : (widget.max != null && widget.min != null) ? (widget.max - widget.min) / 100
                        : 1;
                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context) ? content
                        : new Actions(
                            actions -> {
                                actions.addAction(ActionTrigger.POSITIVE_DIRECTIONS, () -> applyValue(widget.value + step));
                                actions.addAction(ActionTrigger.NEGATIVE_DIRECTIONS, () -> applyValue(widget.value - step));
                            },
                            new MouseArea(
                                mouseArea -> mouseArea
                                    .dragCallback((x, y, dx, dy) -> {
                                        if (widget.axis == LayoutAxis.VERTICAL) dy = -dy;
                                        this.dragValue += this.widget().axis.choose(dx, dy) /*/ (constraints.maxFiniteOrMinOnAxis(this.widget().axis))*/;
                                        this.applyValue(this.dragValue);
                                    })
                                    .scrollCallback(((horizontal, vertical) -> {
                                        //TODO: negate horizontal scrolling in appstate?
                                        var offset = Math.abs(vertical) > Math.abs(horizontal) ? vertical : -horizontal;
                                        return applyValue(widget.value + offset * step);
                                    }))
                                    .cursorStyle(widget.axis.choose(CursorStyle.HORIZONTAL_RESIZE, CursorStyle.VERTICAL_RESIZE)),
                                content
                            )
                        )
                );
            });
        }

        protected boolean applyValue(double value) {
            var widget = this.widget();
            var normalized = normalizeValue(value, widget.min, widget.max, widget.wrap);
            if (normalized == widget.normalizedValue) return false;
            var trueMin = widget.min != null ? widget.min : Double.NEGATIVE_INFINITY;
            var trueMax = widget.max != null ? widget.max : Double.POSITIVE_INFINITY;
            var newValue = trueMin + normalized * (trueMax - trueMin);
            var step = widget.step;
            if (step != null) newValue = Math.round(newValue / step) * step;
            if (widget.value == newValue) return false;
            widget.onChanged.accept(newValue);
            return true;
        }
    }

    protected static double normalizeValue(
        double value,
        @Nullable Double min,
        @Nullable Double max,
        boolean wrap
    ) {
        double trueMin = min != null ? min : Double.NEGATIVE_INFINITY;
        double trueMax = max != null ? max : Double.POSITIVE_INFINITY;

        var normalized = (value - trueMin) / (trueMax - trueMin);
        if (Double.isNaN(normalized)) normalized = 0;

        if (wrap && min != null && max != null) {
            normalized = normalized - Math.floor(normalized);
        } else {
            normalized = MathHelper.clamp(normalized, 0, 1);
        }
        return normalized;
    }
}
