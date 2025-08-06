package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class RawSlider extends StatefulWidget {

    public final double value;
    public final double min;
    public final double max;
    public final @Nullable Double step;
    public final double normalizedValue;
    public final LayoutAxis axis;

    public final @Nullable DoubleConsumer onChanged;
    public final @Nullable Widget track;
    public final Widget handle;
    public final double handleSize;

    public RawSlider(
        double value,
        double min,
        double max,
        @Nullable Double step,
        LayoutAxis axis,
        @Nullable DoubleConsumer onChanged,
        @Nullable Widget track,
        Widget handle,
        double handleSize
    ) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.normalizedValue = MathHelper.clamp((value - min) / (max - min), 0, 1);
        this.axis = axis;
        this.onChanged = onChanged;
        this.track = track;
        this.handle = handle;
        this.handleSize = handleSize;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawSlider> {

        protected double dragValue = 0;

        @Override
        public Widget build(BuildContext context) {
            return new LayoutBuilder((innerContext, constraints) -> {
                var widget = this.widget();
                var size = constraints.maxFiniteOrMinSize();
                var step = widget.step != null ? widget.step : (widget.max - widget().min) / 100;
                var content = new Stack(
                    widget.axis.choose(Alignment.LEFT, Alignment.TOP),
                    new Sized(
                        size.width(),
                        size.height(),
                        widget.track
                    ),
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
                    widget.onChanged == null
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
