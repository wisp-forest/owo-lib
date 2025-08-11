package io.wispforest.owo.braid.widgets.slider.drag;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.LayoutBuilder;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

/// A widget that allows dragging a value along a specified axis.
///
/// @author chyzman
public class RawDrag extends StatefulWidget {

    /// The current value of this drag.
    public final double value;
    private final double normalizedValue;
    /// The minimum value for this drag. If null, there is no minimum.
    private @Nullable Double min = 0d;
    /// The maximum value for this drag. If null, there is no maximum.
    private @Nullable Double max = 1d;
    /// The step size for this drag. If null, the drag is continuous.
    private @Nullable Double step = null;
    /// Whether the value should wrap around when exceeding the min or max.
    private boolean wrap = false;

    /// The axis along which the drag is draggable.
    private LayoutAxis axis = LayoutAxis.HORIZONTAL;

    /// A callback that is invoked when this drag's value changes.
    private @Nullable DoubleConsumer onChanged = null;
    /// The child widget to display inside this drag.
    public final @Nullable Widget child;


    /// Constructs a new `RawDrag` widget.
    ///
    /// @param value         The initial value for this Drag.
    /// @param setupCallback A callback used to configure this Drag's properties, including:
    ///                      <ul>
    ///                      <li>{@link #min(Double)}: Minimum value</li>
    ///                      <li>{@link #max(Double)}: Maximum value</li>
    ///                      <li>{@link #step(Double)}: Step size</li>
    ///                      <li>{@link #wrap(boolean)}: Whether to wrap the value around when it exceeds the bounds</li>
    ///                      <li>{@link #onChanged(DoubleConsumer)}: Callback for value changes</li>
    ///                      <li>{@link #axis(LayoutAxis)}: Drag axis</li>
    ///                      </ul>
    /// @param child         this Drag's child
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

    /// Sets the minimum value for this Drag.
    ///
    /// @param min The minimum value, or `null` to remove it.
    /// @apiNote Providing a higher min than maximum can be used to invert the drag's direction.
    /// @see #max(Double)
    /// @see #clamp(Double, Double)
    public RawDrag min(@Nullable Double min) {
        this.assertMutable();
        this.min = min;
        return this;
    }

    /// Sets the minimum value for this Drag.
    ///
    /// @param min The minimum value.
    /// @apiNote Providing a higher min than maximum can be used to invert the drag's direction.
    /// @see #min(Double)
    public RawDrag min(double min) {
        this.assertMutable();
        this.min = min;
        return this;
    }

    /// @return The minimum value for this Drag, or `null` if there is none.
    /// @see #min(Double)
    public @Nullable Double min() {
        return this.min;
    }

    /// Sets the maximum value for this Drag.
    ///
    /// @param max The maximum value, or `null` to remove it.
    /// @apiNote Providing a lower maximum than minimum can be used to invert the drag's direction.
    /// @see #min(Double)
    /// @see #clamp(Double, Double)
    public RawDrag max(@Nullable Double max) {
        this.assertMutable();
        this.max = max;
        return this;
    }

    /// Sets the maximum value for this Drag.
    ///
    /// @param max The maximum value.
    /// @apiNote Providing a lower maximum than minimum can be used to invert the drag's direction.
    /// @see #max(Double)
    public RawDrag max(double max) {
        this.assertMutable();
        this.max = max;
        return this;
    }

    /// @return The maximum value for this Drag, or `null` if there is none.
    /// @see #max(Double)
    public @Nullable Double max() {
        return this.max;
    }

    /// Sets both the minimum and maximum values of the Drag.
    ///
    /// @param min The minimum value, or `null` to remove it.
    /// @param max The maximum value, or `null` to remove it.
    /// @apiNote Providing a lower maximum than minimum can be used to invert the drag's direction.
    /// @see #min(Double)
    /// @see #max(Double)
    public RawDrag clamp(@Nullable Double min, @Nullable Double max) {
        this.assertMutable();
        this.min = min;
        this.max = max;
        return this;
    }

    /// Sets both the minimum and maximum values for this Drag.
    ///
    /// @param min The minimum value.
    /// @param max The maximum value.
    /// @apiNote Providing a lower maximum than minimum can be used to invert the drag's direction.
    /// @see #clamp(Double, Double)
    public RawDrag clamp(double min, double max) {
        this.assertMutable();
        this.min = min;
        this.max = max;
        return this;
    }

    /// Sets the step size for this Drag.
    ///
    /// @param step The step size, or `null` for a continuous drag.
    public RawDrag step(@Nullable Double step) {
        this.assertMutable();
        this.step = step;
        return this;
    }

    /// @return The step size of this Drag, or `null` if it is continuous.
    /// @see #step(Double)
    public @Nullable Double step() {
        return this.step;
    }

    /// Sets whether the drag's value wraps around when exceeding the min or max.
    ///
    /// @param wrap Whether to wrap this drag's value.
    public RawDrag wrap(boolean wrap) {
        this.assertMutable();
        this.wrap = wrap;
        return this;
    }

    /// @return `true` if the value wraps around,`false` otherwise
    /// @see #wrap(boolean)
    public boolean wrap() {
        return this.wrap;
    }

    /// Sets the callback to be invoked when this drag's value changes.
    ///
    /// @param onChanged The callback, or `null` to deactivate this Drag.
    public RawDrag onChanged(@Nullable DoubleConsumer onChanged) {
        this.assertMutable();
        this.onChanged = onChanged;
        return this;
    }

    /// @return The callback invoked on value changes, or `null` if this Drag is inactive.
    /// @see #onChanged(DoubleConsumer)
    public @Nullable DoubleConsumer onChanged() {
        return this.onChanged;
    }

    /// Sets the axis along which this drag is draggable.
    ///
    /// @param axis The drag axis.
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
                var step = widget.step != null
                    ? widget.step
                    : (widget.max != null && widget.min != null)
                        ? (widget.max - widget.min) / 100
                        : 1;
                var content = new Sized(size, widget.child);
                return new Center(
                    widget.onChanged == null
                        ? content
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
