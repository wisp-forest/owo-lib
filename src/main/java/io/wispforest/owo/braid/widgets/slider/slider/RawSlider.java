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
import io.wispforest.owo.braid.widgets.slider.SliderCallback;
import io.wispforest.owo.braid.widgets.slider.ValueMapper;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class RawSlider extends StatefulWidget {

    public final double value;
    private double min = 0;
    private double max = 1;
    private @Nullable Double step;
    private LayoutAxis axis = LayoutAxis.HORIZONTAL;
    private ValueMapper valueMapper = ValueMapper.LINEAR;
    private @Nullable SliderCallback onChanged;
    private @Nullable Widget track;
    public final Widget handle;
    private double handleSize = 8;

    private final double normalizedValue;
    private Double incrementStep = null;

    public RawSlider(
        double value,
        Widget handle,
        WidgetSetupCallback<RawSlider> setupCallback
    ) {
        this.value = value;
        this.handle = handle;
        setupCallback.setup(this);
        this.normalizedValue = this.valueMapper.toNormalized(value, this.min, this.max);
        if (incrementStep == null) this.incrementStep = this.step != null ? this.step : (this.max - this.min) / 100;
    }

    //region setters/getters

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

    public @Nullable Double step() {
        return this.step;
    }

    public RawSlider axis(LayoutAxis axis) {
        this.assertMutable();
        this.axis = axis;
        return this;
    }

    public LayoutAxis axis() {
        return this.axis;
    }

    public RawSlider valueMapper(ValueMapper valueMapper) {
        this.assertMutable();
        this.valueMapper = valueMapper;
        return this;
    }

    public ValueMapper valueMapper() {
        return this.valueMapper;
    }

    public RawSlider onChanged(@Nullable SliderCallback onChanged) {
        this.assertMutable();
        this.onChanged = onChanged;
        return this;
    }

    public @Nullable SliderCallback onChanged() {
        return this.onChanged;
    }

    public RawSlider track(@Nullable Widget track) {
        this.assertMutable();
        this.track = track;
        return this;
    }

    public @Nullable Widget track() {
        return this.track;
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
                            actions -> actions
                                .addAction(ActionTrigger.POSITIVE_DIRECTIONS, () -> applyValue(widget.value + widget.incrementStep))
                                .addAction(ActionTrigger.NEGATIVE_DIRECTIONS, () -> applyValue(widget.value - widget.incrementStep)),
                            new MouseArea(
                                mouseArea -> mouseArea
                                    //TODO: decide what to do with buttons here
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;

                                        y = widget.axis == LayoutAxis.VERTICAL ? constraints.maxFiniteOrMinOnAxis(widget.axis) - y : y;
                                        var initialDragValue = widget.normalizedValue;
                                        if (!this.isInHandle(constraints, x, y))
                                            initialDragValue = this.setAbsolute(constraints, x, y);

                                        this.dragValue = initialDragValue;
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> this.move(constraints, dx, widget.axis == LayoutAxis.VERTICAL ? -dy : dy))
                                    .scrollCallback((horizontal, vertical) -> {
                                        //TODO: negate horizontal scrolling in appstate?
                                        var offset = Math.abs(vertical) > Math.abs(horizontal) ? vertical : -horizontal;
                                        return applyValue(widget.value + offset * widget.incrementStep);
                                    })
                                    .cursorStyle(CursorStyle.HAND),
                                content
                            )
                        )
                );
            });
        }

        protected boolean isInHandle(Constraints constraints, double x, double y) {
            var widget = this.widget();
            var axis = widget.axis;

            var trackLength = constraints.maxFiniteOrMinOnAxis(axis) - widget.handleSize;
            var handleMin = widget.normalizedValue * trackLength;
            var handleMax = handleMin + widget.handleSize;

            var coordinate = axis.choose(x, y);
            return coordinate >= handleMin && coordinate <= handleMax;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            var widget = this.widget();
            this.dragValue += widget.axis.choose(dx, dy) / (constraints.maxFiniteOrMinOnAxis(widget.axis) - widget.handleSize);
            this.applyValue(widget.valueMapper.fromNormalized(MathHelper.clamp(this.dragValue, 0, 1), widget.min, widget.max));
        }

        protected double setAbsolute(Constraints constraints, double x, double y) {
            var widget = this.widget();
            if (widget.onChanged == null) return widget.normalizedValue;

            var axis = widget.axis;
            var handleSize = widget.handleSize;

            var newNormalizedValue = MathHelper.clamp((axis.choose(x, y) - handleSize / 2) / (constraints.maxFiniteOrMinOnAxis(axis) - handleSize), 0, 1);

            this.applyValue(widget().valueMapper.fromNormalized(newNormalizedValue, widget().min, widget().max));
            return newNormalizedValue;
        }

        protected boolean applyValue(double newValue) {
            var widget = this.widget();
            if (widget.onChanged == null) return false;
            if (widget.step != null) newValue = Math.round(newValue / widget.step) * widget.step;
            newValue = MathHelper.clamp(newValue, widget.min, widget.max);
            if (newValue == widget.value) return false;
            widget.onChanged.accept(newValue);
            return true;
        }
    }
}
