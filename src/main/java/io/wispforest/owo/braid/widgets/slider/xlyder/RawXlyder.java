package io.wispforest.owo.braid.widgets.slider.xlyder;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.slider.ValueMapper;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class RawXlyder extends StatefulWidget {

    public final Vector2dc value;
    protected final Vector2d min = new Vector2d();
    protected final Vector2d max = new Vector2d(1);
    protected @Nullable Double xStep;
    protected @Nullable Double yStep;
    protected ValueMapper xValueMapper = ValueMapper.LINEAR;
    protected ValueMapper yValueMapper = ValueMapper.LINEAR;
    protected Size handleSize;

    public final @Nullable XlyderCallback onChanged;
    public final @Nullable Widget track;
    public final Widget handle;

    protected @Nullable Double xIncrementStep = null;
    protected @Nullable Double yIncrementStep = null;

    public RawXlyder(
        Vector2dc value,
        @Nullable XlyderSetupCallback<RawXlyder> setupCallback,
        @Nullable XlyderCallback onChanged,
        @Nullable Widget track,
        Size handleSize,
        Widget handle
    ) {
        this.value = value;
        this.onChanged = onChanged;
        this.track = track;
        this.handleSize = handleSize;
        this.handle = handle;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public RawXlyder(
        Vector2dc value,
        @Nullable XlyderSetupCallback<RawXlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged,
        @Nullable Widget track,
        Size handleSize,
        Widget handle
    ) {
        this(value, setupCallback, active ? onChanged : null, track, handleSize, handle);
    }

    public RawXlyder(
        double x, double y,
        @Nullable XlyderSetupCallback<RawXlyder> setupCallback,
        @Nullable XlyderCallback onChanged,
        @Nullable Widget track,
        Size handleSize,
        Widget handle
    ) {
        this(new Vector2d(x, y), setupCallback, onChanged, track, handleSize, handle);
    }

    public RawXlyder(
        double x, double y,
        @Nullable XlyderSetupCallback<RawXlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged,
        @Nullable Widget track,
        Size handleSize,
        Widget handle
    ) {
        this(new Vector2d(x, y), setupCallback, active ? onChanged : null, track, handleSize, handle);
    }



    //region Setup Methods

    public RawXlyder min(Vector2d min) {
        this.assertMutable();
        this.min.set(min);
        return this;
    }

    public RawXlyder min(double minX, double minY) {
        this.assertMutable();
        this.min.set(minX, minY);
        return this;
    }

    public RawXlyder min(double min) {
        this.assertMutable();
        this.min.set(min, min);
        return this;
    }

    public Vector2dc min() {
        return this.min;
    }

    public RawXlyder minX(double minX) {
        this.assertMutable();
        this.min.x = minX;
        return this;
    }

    public double minX() {
        return this.min.x;
    }

    public RawXlyder minY(double minY) {
        this.assertMutable();
        this.min.y = minY;
        return this;
    }

    public double minY() {
        return this.min.y;
    }

    public RawXlyder max(Vector2d max) {
        this.assertMutable();
        this.max.set(max);
        return this;
    }

    public RawXlyder max(double maxX, double maxY) {
        this.assertMutable();
        this.max.set(maxX, maxY);
        return this;
    }

    public RawXlyder max(double max) {
        this.assertMutable();
        this.max.set(max, max);
        return this;
    }

    public Vector2dc max() {
        return this.max;
    }

    public RawXlyder maxX(double maxX) {
        this.assertMutable();
        this.max.x = maxX;
        return this;
    }

    public double maxX() {
        return this.max.x;
    }

    public RawXlyder maxY(double maxY) {
        this.assertMutable();
        this.max.y = maxY;
        return this;
    }

    public double maxY() {
        return this.max.y;
    }

    public RawXlyder range(Vector2d min, Vector2d max) {
        this.assertMutable();
        this.min.set(min);
        this.max.set(max);
        return this;
    }

    public RawXlyder range(double minX, double minY, double maxX, double maxY) {
        this.assertMutable();
        this.min.set(minX, minY);
        this.max.set(maxX, maxY);
        return this;
    }

    public RawXlyder range(double min, double max) {
        this.assertMutable();
        this.min.set(min, min);
        this.max.set(max, max);
        return this;
    }

    public RawXlyder rangeX(double minX, double maxX) {
        this.assertMutable();
        this.min.x = minX;
        this.max.x = maxX;
        return this;
    }

    public RawXlyder rangeY(double minY, double maxY) {
        this.assertMutable();
        this.min.y = minY;
        this.max.y = maxY;
        return this;
    }

    public RawXlyder step(@Nullable Double step) {
        this.assertMutable();
        this.xStep = step;
        this.yStep = step;
        return this;
    }

    public RawXlyder step(double step) {
        this.assertMutable();
        this.xStep = step;
        this.yStep = step;
        return this;
    }

    public RawXlyder stepX(@Nullable Double xStep) {
        this.assertMutable();
        this.xStep = xStep;
        return this;
    }

    public RawXlyder stepX(double xStep) {
        this.assertMutable();
        this.xStep = xStep;
        return this;
    }

    public @Nullable Double stepX() {
        return this.xStep;
    }

    public RawXlyder stepY(@Nullable Double yStep) {
        this.assertMutable();
        this.yStep = yStep;
        return this;
    }

    public RawXlyder stepY(double yStep) {
        this.assertMutable();
        this.yStep = yStep;
        return this;
    }

    public @Nullable Double stepY() {
        return this.yStep;
    }

    public RawXlyder valueMapper(ValueMapper valueMapper) {
        this.assertMutable();
        this.xValueMapper = valueMapper;
        this.yValueMapper = valueMapper;
        return this;
    }

    public RawXlyder valueMapperX(ValueMapper xValueMapper) {
        this.assertMutable();
        this.xValueMapper = xValueMapper;
        return this;
    }

    public ValueMapper valueMapperX() {
        return this.xValueMapper;
    }

    public RawXlyder valueMapperY(ValueMapper yValueMapper) {
        this.assertMutable();
        this.yValueMapper = yValueMapper;
        return this;
    }

    public ValueMapper valueMapperY() {
        return this.yValueMapper;
    }

    public RawXlyder incrementStep(@Nullable Double incrementStep) {
        this.assertMutable();
        this.xIncrementStep = incrementStep;
        this.yIncrementStep = incrementStep;
        return this;
    }

    public RawXlyder incrementStep(double incrementStep) {
        this.assertMutable();
        this.xIncrementStep = incrementStep;
        this.yIncrementStep = incrementStep;
        return this;
    }

    public RawXlyder incrementStepX(@Nullable Double xIncrementStep) {
        this.assertMutable();
        this.xIncrementStep = xIncrementStep;
        return this;
    }

    public RawXlyder incrementStepX(double xIncrementStep) {
        this.assertMutable();
        this.xIncrementStep = xIncrementStep;
        return this;
    }

    public @Nullable Double incrementStepX() {
        return this.xIncrementStep;
    }

    public RawXlyder incrementStepY(@Nullable Double yIncrementStep) {
        this.assertMutable();
        this.yIncrementStep = yIncrementStep;
        return this;
    }

    public RawXlyder incrementStepY(double yIncrementStep) {
        this.assertMutable();
        this.yIncrementStep = yIncrementStep;
        return this;
    }

    public @Nullable Double incrementStepY() {
        return this.yIncrementStep;
    }

    //endregion

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawXlyder> {

        protected final Vector2d dragValue = new Vector2d();
        protected boolean dragging = false;

        protected Vector2dc normalizedValue;
        protected Vector2dc incrementStep;

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            this.normalizedValue = new Vector2d(
                widget.xValueMapper.normalize(widget.value.x(), widget.min.x, widget.max.x),
                widget.yValueMapper.normalize(widget.value.y(), widget.min.y, widget.max.y)
            );
            this.incrementStep = new Vector2d(
                widget.xIncrementStep != null ? widget.xValueMapper.normalize(widget.xIncrementStep, widget.min.x, widget.max.x) : widget.xStep != null ? widget.xValueMapper.normalize(widget.xStep, widget.min.x, widget.max.x) : 0.01,
                widget.yIncrementStep != null ? widget.yValueMapper.normalize(widget.yIncrementStep, widget.min.y, widget.max.y) : widget.yStep != null ? widget.yValueMapper.normalize(widget.yStep, widget.min.y, widget.max.y) : 0.01
            );
            return new LayoutBuilder((innerContext, constraints) -> {
                var content = new Stack(
                    Alignment.TOP_LEFT,
                    new Sized(
                        constraints.maxWidth(),
                        constraints.maxHeight(),
                        widget.track
                    ),
                    new Padding(
                        Insets.left(Math.floor((constraints.maxWidth() - widget.handleSize.width()) * normalizedValue.x()))
                            .withTop(Math.floor((constraints.maxHeight() - widget.handleSize.height()) * (1 - normalizedValue.y()))),
                        new Sized(
                            widget.handleSize,
                            widget.handle
                        )
                    )
                );
                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Incrementor(
                            xIncrement -> applyValue(MathHelper.clamp(normalizedValue.x() + incrementStep.x() * xIncrement, 0, 1), null),
                            yIncrement -> applyValue(null, MathHelper.clamp(normalizedValue.y() + incrementStep.y() * yIncrement, 0, 1)),
                            new MouseArea(
                                mouseArea -> mouseArea
                                    //TODO: decide what to do with buttons here
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;

                                        y = constraints.maxHeight() - y;
                                        Vector2dc initialDragValue = new Vector2d(normalizedValue);
                                        if (!this.isInHandle(constraints, x, y)) initialDragValue = this.setAbsolute(constraints, x, y);

                                        this.dragValue.set(initialDragValue);
                                        this.dragging = true;
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> this.move(constraints, dx, -dy))
                                    .releaseCallback((x, y, button, modifiers) -> this.dragging = false)
                                    //TODO: invert the y passed here cuz it cringe atm
                                    .cursorStyleSupplier((x, y) -> (!isInHandle(constraints, x, constraints.maxHeight() - y) && !dragging) ? CursorStyle.HAND : CursorStyle.MOVE),
                                content
                            )
                        )
                );
            });
        }

        protected boolean isInHandle(Constraints constraints, double x, double y) {
            var trackWidth = constraints.maxWidth() - this.widget().handleSize.width();
            var trackHeight = constraints.maxHeight() - this.widget().handleSize.height();

            var handleMinX = normalizedValue.x() * trackWidth;
            var handleMinY = normalizedValue.y() * trackHeight;
            var handleMaxX = handleMinX + this.widget().handleSize.width();
            var handleMaxY = handleMinY + this.widget().handleSize.height();

            return x >= handleMinX && x <= handleMaxX && y >= handleMinY && y <= handleMaxY;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            this.dragValue.add(
                dx / (constraints.maxWidth() - this.widget().handleSize.width()),
                dy / (constraints.maxHeight() - this.widget().handleSize.height())
            );

            this.applyValue(
                MathHelper.clamp(this.dragValue.x, 0, 1),
                MathHelper.clamp(this.dragValue.y, 0, 1)
            );
        }

        protected Vector2dc setAbsolute(Constraints constraints, double x, double y) {
            if (this.widget().onChanged == null) return this.normalizedValue;

            var handleSize = this.widget().handleSize;

            var newNormalizedX = MathHelper.clamp((x - (handleSize.width() / 2)) / (constraints.maxWidth() - handleSize.width()), 0, 1);
            var newNormalizedY = MathHelper.clamp((y - (handleSize.height() / 2)) / (constraints.maxHeight() - handleSize.height()), 0, 1);

            this.applyValue(newNormalizedX, newNormalizedY);
            return new Vector2d(newNormalizedX, newNormalizedY);
        }

        protected void applyValue(@Nullable Double newNormalizedX, @Nullable Double newNormalizedY) {
            if (newNormalizedX == null && newNormalizedY == null) return;
            var widget = this.widget();
            double newX = widget.value.x();
            double newY = widget.value.y();
            if (newNormalizedX != null) newX = widget.xValueMapper.deNormalize(newNormalizedX, widget.min.x, widget.max.x);
            if (newNormalizedY != null) newY = widget.yValueMapper.deNormalize(newNormalizedY, widget.min.y, widget.max.y);
            widget.onChanged.accept(newX, newY);
        }
    }

    @FunctionalInterface
    public interface XlyderSetupCallback<T extends RawXlyder> extends WidgetSetupCallback<T> {
        @Override
        void setup(T xlyder);
    }
}
