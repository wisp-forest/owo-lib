package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public class RawXlyder extends StatefulWidget {

    public final double xValue, yValue;
    public final double minX, minY;
    public final double maxX, maxY;
    public final @Nullable Double xStep, yStep;
    public final double normalizedXValue, normalizedYValue;

    public final XlyderCallback onChanged;
    public final @Nullable Widget track;
    public final Widget handle;
    public final Size handleSize;

    public RawXlyder(
        double xValue, double yValue,
        double minX, double minY,
        double maxX, double maxY,
        @Nullable Double xStep, @Nullable Double yStep,
        XlyderCallback onChanged,
        @Nullable Widget track,
        Widget handle,
        Size handleSize
    ) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.xStep = xStep;
        this.yStep = yStep;
        this.normalizedXValue = MathHelper.clamp((xValue - minX) / (maxX - minX), 0, 1);
        this.normalizedYValue = MathHelper.clamp((yValue - minY) / (maxY - minY), 0, 1);
        this.onChanged = onChanged;
        this.track = track;
        this.handle = handle;
        this.handleSize = handleSize;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawXlyder> {

        protected double dragValueX = 0;
        protected double dragValueY = 0;

        @Override
        public Widget build(BuildContext context) {
            return new LayoutBuilder((innerContext, constraints) -> {
                var widget = this.widget();
                var xStep = widget.xStep != null ? widget.xStep : (widget.maxX - widget.minX) / 100;
                var yStep = widget.yStep != null ? widget.yStep : (widget.maxY - widget.minY) / 100;

                var content = new Stack(
                    Alignment.TOP_LEFT,
                    new Sized(
                        constraints.maxWidth(),
                        constraints.maxHeight(),
                        widget.track
                    ),
                    new Padding(
                        Insets.left(Math.floor((constraints.maxWidth() - widget.handleSize.width()) * widget.normalizedXValue))
                            .withTop(Math.floor((constraints.maxHeight() - widget.handleSize.height()) * (1 - widget.normalizedYValue))),
                        new Sized(
                            widget.handleSize,
                            widget.handle
                        )
                    )
                );
                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Actions(
                            actions -> {
                                actions.addAction(ActionTrigger.UP, () -> widget.onChanged.accept(widget.xValue, Math.min(widget.yValue + yStep, widget.maxY)));
                                actions.addAction(ActionTrigger.DOWN, () -> widget.onChanged.accept(widget.xValue, Math.max(widget.yValue - yStep, widget.minY)));
                                actions.addAction(ActionTrigger.RIGHT, () -> widget.onChanged.accept(Math.min(widget.xValue + xStep, widget.maxX), widget.yValue));
                                actions.addAction(ActionTrigger.LEFT, () -> widget.onChanged.accept(Math.max(widget.xValue - xStep, widget.minX), widget.yValue));
                            },
                            new MouseArea(
                                mouseArea -> mouseArea
                                    //TODO: decide what to do with buttons here
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;

                                        y = constraints.maxHeight() - y;
                                        var initialDragValue = new Vector2d(widget.normalizedXValue, widget.normalizedYValue);
                                        if (!this.isInHandle(constraints, x, y)) {
                                            initialDragValue = this.setAbsolute(constraints, x, y);
                                        }

                                        this.dragValueX = initialDragValue.x;
                                        this.dragValueY = initialDragValue.y;
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> this.move(constraints, dx, -dy))
                                    .scrollCallback((horizontal, vertical) -> {
                                        //TODO: move shift logic to appstate
                                        // Singleton usage spotted :alarm: :alarm:
                                        var offsetX = (Screen.hasShiftDown() ? vertical : -horizontal) * xStep;
                                        var offsetY = (Screen.hasShiftDown() ? -horizontal : vertical) * yStep;
                                        var newX = MathHelper.clamp(widget.xValue + offsetX, widget.minX, widget.maxX);
                                        var newY = MathHelper.clamp(widget.yValue + offsetY, widget.minY, widget.maxY);
                                        if (widget.xValue == newX && widget.yValue == newY) return false;
                                        widget.onChanged.accept(newX, newY);
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
            var trackWidth = constraints.maxWidth() - this.widget().handleSize.width();
            var trackHeight = constraints.maxHeight() - this.widget().handleSize.height();

            var handleMinX = (trackWidth * this.widget().normalizedXValue) + (this.widget().handleSize.width() / 2);
            var handleMinY = (trackHeight * this.widget().normalizedYValue) + (this.widget().handleSize.height() / 2);
            var handleMaxX = handleMinX + this.widget().handleSize.width();
            var handleMaxY = handleMinY + this.widget().handleSize.height();

            return x >= handleMinX && x <= handleMaxX && y >= handleMinY && y <= handleMaxY;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            this.dragValueX += dx / (constraints.maxWidth() - this.widget().handleSize.width());
            this.dragValueY += dy / (constraints.maxHeight() - this.widget().handleSize.height());

            this.applyValue(
                MathHelper.clamp(this.dragValueX, 0, 1),
                MathHelper.clamp(this.dragValueY, 0, 1)
            );
        }

        Vector2d setAbsolute(Constraints constraints, double x, double y) {
            if (this.widget().onChanged == null) return new Vector2d(this.widget().normalizedXValue, this.widget().normalizedYValue);

            var handleWidth = this.widget().handleSize.width();
            var handleHeight = this.widget().handleSize.height();

            var newNormalizedX = MathHelper.clamp((x - (handleWidth / 2)) / (constraints.maxWidth() - handleWidth), 0, 1);
            var newNormalizedY = MathHelper.clamp((y - (handleHeight / 2)) / (constraints.maxHeight() - handleHeight), 0, 1);

            this.applyValue(newNormalizedX, newNormalizedY);
            return new Vector2d(newNormalizedX, newNormalizedY);
        }

        void applyValue(double newNormalizedX, double newNormalizedY) {
            var xStep = this.widget().xStep;
            var yStep = this.widget().yStep;
            var newXValue = this.widget().minX + newNormalizedX * (this.widget().maxX - this.widget().minX);
            var newYValue = this.widget().minY + newNormalizedY * (this.widget().maxY - this.widget().minY);
            this.widget().onChanged.accept(
                xStep != null ? MathHelper.clamp(Math.round(newXValue / xStep) * xStep, this.widget().minX, this.widget().maxX) : newXValue,
                yStep != null ? MathHelper.clamp(Math.round(newYValue / yStep) * yStep, this.widget().minY, this.widget().maxY) : newYValue
            );
        }
    }

    @FunctionalInterface
    public interface XlyderCallback {
        void accept(double x, double y);
    }
}
