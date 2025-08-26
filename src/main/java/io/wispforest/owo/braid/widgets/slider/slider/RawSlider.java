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
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.slider.ValueMapper;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class RawSlider extends StatefulWidget {

    public final double value;
    private double min = 0;
    private double max = 1;
    private @Nullable Double step;
    private ValueMapper valueMapper = ValueMapper.LINEAR;
    private LayoutAxis axis = LayoutAxis.HORIZONTAL;
    private double handleSize = 8;

    public final @Nullable SliderCallback onChanged;
    public final @Nullable Widget track;
    public final Widget handle;

    public final double normalizedValue;
    private Double incrementStep = null;

    public RawSlider(
        double value,
        WidgetSetupCallback<RawSlider> setupCallback,
        @Nullable SliderCallback onChanged,
        @Nullable Widget track,
        Widget handle
    ) {
        this.value = value;
        this.onChanged = onChanged;
        this.track = track;
        this.handle = handle;
        setupCallback.setup(this);

        this.normalizedValue = MathHelper.clamp(this.valueMapper.normalize(value, this.min, this.max), 0, 1);
        if (this.incrementStep == null) incrementStep = step != null ? step : (max - min) / 100;
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
        return this.step((Double) step);
    }

    public @Nullable Double step() {
        return this.step;
    }

    public RawSlider valueMapper(ValueMapper valueMapper) {
        this.assertMutable();
        this.valueMapper = valueMapper;
        return this;
    }

    public ValueMapper valueMapper() {
        return this.valueMapper;
    }

    public RawSlider axis(LayoutAxis axis) {
        this.assertMutable();
        this.axis = axis;
        return this;
    }

    public LayoutAxis axis() {
        return this.axis;
    }

    public RawSlider handleSize(double handleSize) {
        this.assertMutable();
        this.handleSize = handleSize;
        return this;
    }

    public double handleSize() {
        return this.handleSize;
    }

    //endregion

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawSlider> {

        protected double dragValue = 0;

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            return new LayoutBuilder((innerContext, constraints) -> {
                var size = constraints.maxFiniteOrMinSize();
                var content = new Stack(
                    widget.axis.choose(Alignment.LEFT, Alignment.TOP),
                    new Sized(size, widget.track),
                    new Padding(
                        widget.axis.chooseCompute(
                            () -> Insets.left(Math.floor((size.width() - widget.handleSize) * widget.normalizedValue)),
                            () -> Insets.top(Math.floor((size.height() - widget.handleSize) * (1 - widget.normalizedValue)))
                        ),
                        widget.axis.chooseCompute(
                            () -> new Sized(
                                widget.handleSize,
                                size.height(),
                                widget.handle
                            ),
                            () -> new Sized(
                                size.width(),
                                widget.handleSize,
                                widget.handle
                            )
                        )
                    )
                );
                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Actions(
                            actions -> {
                                actions.addAction(ActionTrigger.POSITIVE_DIRECTIONS, () -> widget.onChanged.accept(Math.min(widget.value + step, widget.max)));
                                actions.addAction(ActionTrigger.NEGATIVE_DIRECTIONS, () -> widget.onChanged.accept(Math.max(widget.value - step, widget.min)));
                            },
                            new MouseArea(
                                mouseArea -> mouseArea
                                    //TODO: decide what to do with buttons here
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;

                                        y = widget.axis == LayoutAxis.VERTICAL ? constraints.maxFiniteOrMinOnAxis(widget.axis) - y : y;
                                        var initialDragValue = widget.normalizedValue;
                                        if (!this.isInHandle(constraints, x, y)) initialDragValue = this.setAbsolute(constraints, x, y);

                                        this.dragValue = initialDragValue;
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> this.move(constraints, dx, widget.axis == LayoutAxis.VERTICAL ? -dy : dy))
                                    .scrollCallback((horizontal, vertical) -> {
                                        //TODO: Singleton usage spotted :alarm: :alarm:
                                        var offset = Math.abs(vertical) > Math.abs(horizontal) ? vertical : -horizontal;
                                        var newValue = MathHelper.clamp(widget.value + offset * step, widget.min, widget.max);
                                        if (widget.value == newValue) return false;
                                        widget.onChanged.accept(newValue);
                                        return true;
                                    })
                                    .cursorStyle(CursorStyle.HAND),
                                content
                            )
                        )
                );
            });
        }

        protected boolean isInHandle(Constraints constraints, double x, double y) {
            var axis = this.widget().axis;

            var trackLength = constraints.maxFiniteOrMinOnAxis(axis) - this.widget().handleSize;
            var handleMin = this.widget().normalizedValue * trackLength;
            var handleMax = handleMin + this.widget().handleSize;

            var coordinate = axis.choose(x, y);
            return coordinate >= handleMin && coordinate <= handleMax;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            this.dragValue += this.widget().axis.choose(dx, dy) / (constraints.maxFiniteOrMinOnAxis(this.widget().axis) - this.widget().handleSize);

            this.applyValue(MathHelper.clamp(this.dragValue, 0, 1));
        }

        protected double setAbsolute(Constraints constraints, double x, double y) {
            if (this.widget().onChanged == null) return this.widget().normalizedValue;

            var axis = this.widget().axis;
            var handleSize = this.widget().handleSize;

            var newNormalizedValue = MathHelper.clamp((axis.choose(x, y) - handleSize / 2) / (constraints.maxFiniteOrMinOnAxis(axis) - handleSize), 0, 1);

            this.applyValue(newNormalizedValue);
            return newNormalizedValue;
        }

        protected void applyValue(double newNormalizedValue) {
            var step = this.widget().step;
            var newValue = this.widget().min + newNormalizedValue * (this.widget().max - this.widget().min);
            this.widget().onChanged.accept(step != null ? Math.round(newValue / step) * step : newValue);
        }
    }
}
