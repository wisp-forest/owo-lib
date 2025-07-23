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
import io.wispforest.owo.braid.widgets.stack.Stack;
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
        DoubleConsumer onChanged,
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

                return new Center(
                    new MouseArea(
                        mouseArea -> mouseArea
                            //TODO: decide what to do with buttons here
                            .clickCallback((x, y, button) -> {
                                if (button != 0) return false;

                                y = widget.axis == LayoutAxis.VERTICAL ? constraints.maxOnAxis(widget.axis) - y : y;
                                if (!this.isInHandle(constraints, x, y)) {
                                    this.setAbsolute(constraints, x, y);
                                }

                                return true;
                            })
                            .dragCallback((x, y, dx, dy) -> this.move(constraints, dx, widget.axis == LayoutAxis.VERTICAL ? -dy : dy))
                            .dragStartCallback(button -> this.dragValue = widget.normalizedValue)
                            .cursorStyle(CursorStyle.HAND),
                        new Stack(
                            widget.axis.choose(Alignment.LEFT, Alignment.TOP),
                            new Sized(
                                constraints.maxWidth(),
                                constraints.maxHeight(),
                                widget.track
                            ),
                            new Padding(
                                widget.axis.chooseCompute(
                                    () -> Insets.left(Math.floor((constraints.maxWidth() - widget.handleSize) * widget.normalizedValue)),
                                    () -> Insets.top(Math.floor((constraints.maxHeight() - widget.handleSize) * (1 - widget.normalizedValue)))
                                ),
                                widget.axis.chooseCompute(
                                    () -> new Sized(
                                        widget.handleSize,
                                        constraints.maxHeight(),
                                        widget.handle
                                    ),
                                    () -> new Sized(
                                        constraints.maxWidth(),
                                        widget.handleSize,
                                        widget.handle
                                    )
                                )
                            )
                        )
                    )
                );
            });
        }

        protected boolean isInHandle(Constraints constraints, double x, double y) {
            var axis = this.widget().axis;

            var trackLength = constraints.maxOnAxis(axis) - this.widget().handleSize;
            var handleMin = this.widget().normalizedValue * trackLength;
            var handleMax = handleMin + this.widget().handleSize;

            var coordinate = axis.choose(x, y);
            return coordinate >= handleMin && coordinate <= handleMax;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            this.dragValue += this.widget().axis.choose(dx, dy) / (constraints.maxOnAxis(this.widget().axis) - this.widget().handleSize);

            this.applyValue(MathHelper.clamp(this.dragValue, 0, 1));
        }

        void setAbsolute(Constraints constraints, double x, double y) {
            if (this.widget().onChanged == null) return;

            var axis = this.widget().axis;
            var handleSize = this.widget().handleSize;

            var newNormalizedValue = MathHelper.clamp((axis.choose(x, y) - handleSize / 2) / (constraints.maxOnAxis(axis) - handleSize), 0, 1);

            applyValue(newNormalizedValue);
        }

        void applyValue(double newNormalizedValue) {
            var step = this.widget().step;
            var newValue = this.widget().min + newNormalizedValue * (this.widget().max - this.widget().min);
            this.widget().onChanged.accept(step != null ? Math.round(newValue / step) * step : newValue);
        }
    }
}
