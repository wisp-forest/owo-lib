package io.wispforest.owo.braid.widgets.slider.rangeXlyder;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.slider.slider.SliderFunction;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

/**
 * A slider widget with two handles that can each be independently dragged on both X and Y axes.
 * Combines the dual-handle functionality of RangeSlider with the 2D movement capability of Xlyder.
 */
public class RawRangeXlyder extends StatefulWidget {

    public final Vector2dc minValue, maxValue;
    protected final Vector2d min = new Vector2d();
    protected final Vector2d max = new Vector2d(1);
    protected @Nullable Double xStep;
    protected @Nullable Double yStep;
    protected SliderFunction xSliderFunction = SliderFunction.LINEAR;
    protected SliderFunction ySliderFunction = SliderFunction.LINEAR;
    protected Size minHandleSize, maxHandleSize;

    public final @Nullable RangeXlyderCallback onChanged;
    public final @Nullable Widget track;
    public final Widget minHandle, maxHandle;

    protected @Nullable Double xIncrementStep = null;
    protected @Nullable Double yIncrementStep = null;

    public RawRangeXlyder(
        Vector2dc minValue,
        Vector2dc maxValue,
        @Nullable WidgetSetupCallback<RawRangeXlyder> setupCallback,
        @Nullable RangeXlyderCallback onChanged,
        @Nullable Widget track,
        Size minHandleSize,
        Widget minHandle,
        Size maxHandleSize,
        Widget maxHandle
    ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.onChanged = onChanged;
        this.track = track;
        this.minHandleSize = minHandleSize;
        this.minHandle = minHandle;
        this.maxHandleSize = maxHandleSize;
        this.maxHandle = maxHandle;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public RawRangeXlyder(
        Vector2dc minValue,
        Vector2dc maxValue,
        @Nullable WidgetSetupCallback<RawRangeXlyder> setupCallback,
        boolean active,
        RangeXlyderCallback onChanged,
        @Nullable Widget track,
        Size minHandleSize,
        Widget minHandle,
        Size maxHandleSize,
        Widget maxHandle
    ) {
        this(minValue, maxValue, setupCallback, active ? onChanged : null, track, minHandleSize, minHandle, maxHandleSize, maxHandle);
    }

    public RawRangeXlyder(
        Vector2dc minValue,
        Vector2dc maxValue,
        @Nullable WidgetSetupCallback<RawRangeXlyder> setupCallback,
        @Nullable RangeXlyderCallback onChanged,
        @Nullable Widget track,
        Size handleSize,
        Widget handle
    ) {
        this(minValue, maxValue, setupCallback, onChanged, track, handleSize, handle, handleSize, handle);
    }

    public RawRangeXlyder(
        Vector2dc minValue,
        Vector2dc maxValue,
        @Nullable WidgetSetupCallback<RawRangeXlyder> setupCallback,
        boolean active,
        RangeXlyderCallback onChanged,
        @Nullable Widget track,
        Size handleSize,
        Widget handle
    ) {
        this(minValue, maxValue, setupCallback, active ? onChanged : null, track, handleSize, handle);
    }

    public RawRangeXlyder(
        double minX, double minY, double maxX, double maxY,
        @Nullable WidgetSetupCallback<RawRangeXlyder> setupCallback,
        @Nullable RangeXlyderCallback onChanged,
        @Nullable Widget track,
        Size minHandleSize,
        Widget minHandle,
        Size maxHandleSize,
        Widget maxHandle
    ) {
        this(new Vector2d(minX, minY), new Vector2d(maxX, maxY), setupCallback, onChanged, track, minHandleSize, minHandle, maxHandleSize, maxHandle);
    }

    public RawRangeXlyder(
        double minX, double minY, double maxX, double maxY,
        @Nullable WidgetSetupCallback<RawRangeXlyder> setupCallback,
        boolean active,
        RangeXlyderCallback onChanged,
        @Nullable Widget track,
        Size minHandleSize,
        Widget minHandle,
        Size maxHandleSize,
        Widget maxHandle
    ) {
        this(new Vector2d(minX, minY), new Vector2d(maxX, maxY), setupCallback, active ? onChanged : null, track, minHandleSize, minHandle, maxHandleSize, maxHandle);
    }

    //region Setup Methods

    public RawRangeXlyder min(Vector2d min) {
        this.assertMutable();
        this.min.set(min);
        return this;
    }

    public RawRangeXlyder min(double minX, double minY) {
        this.assertMutable();
        this.min.set(minX, minY);
        return this;
    }

    public RawRangeXlyder min(double min) {
        this.assertMutable();
        this.min.set(min, min);
        return this;
    }

    public Vector2dc min() {
        return this.min;
    }

    public RawRangeXlyder minX(double minX) {
        this.assertMutable();
        this.min.x = minX;
        return this;
    }

    public double minX() {
        return this.min.x;
    }

    public RawRangeXlyder minY(double minY) {
        this.assertMutable();
        this.min.y = minY;
        return this;
    }

    public double minY() {
        return this.min.y;
    }

    public RawRangeXlyder max(Vector2d max) {
        this.assertMutable();
        this.max.set(max);
        return this;
    }

    public RawRangeXlyder max(double maxX, double maxY) {
        this.assertMutable();
        this.max.set(maxX, maxY);
        return this;
    }

    public RawRangeXlyder max(double max) {
        this.assertMutable();
        this.max.set(max, max);
        return this;
    }

    public Vector2dc max() {
        return this.max;
    }

    public RawRangeXlyder maxX(double maxX) {
        this.assertMutable();
        this.max.x = maxX;
        return this;
    }

    public double maxX() {
        return this.max.x;
    }

    public RawRangeXlyder maxY(double maxY) {
        this.assertMutable();
        this.max.y = maxY;
        return this;
    }

    public double maxY() {
        return this.max.y;
    }

    public RawRangeXlyder range(Vector2d min, Vector2d max) {
        this.assertMutable();
        this.min.set(min);
        this.max.set(max);
        return this;
    }

    public RawRangeXlyder range(double minX, double minY, double maxX, double maxY) {
        this.assertMutable();
        this.min.set(minX, minY);
        this.max.set(maxX, maxY);
        return this;
    }

    public RawRangeXlyder step(@Nullable Double step) {
        this.assertMutable();
        this.xStep = step;
        this.yStep = step;
        return this;
    }

    public RawRangeXlyder step(double step) {
        this.assertMutable();
        this.xStep = step;
        this.yStep = step;
        return this;
    }

    public RawRangeXlyder stepX(@Nullable Double xStep) {
        this.assertMutable();
        this.xStep = xStep;
        return this;
    }

    public RawRangeXlyder stepX(double xStep) {
        this.assertMutable();
        this.xStep = xStep;
        return this;
    }

    public @Nullable Double stepX() {
        return this.xStep;
    }

    public RawRangeXlyder stepY(@Nullable Double yStep) {
        this.assertMutable();
        this.yStep = yStep;
        return this;
    }

    public RawRangeXlyder stepY(double yStep) {
        this.assertMutable();
        this.yStep = yStep;
        return this;
    }

    public @Nullable Double stepY() {
        return this.yStep;
    }

    public RawRangeXlyder sliderFunction(SliderFunction sliderFunction) {
        this.assertMutable();
        this.xSliderFunction = sliderFunction;
        this.ySliderFunction = sliderFunction;
        return this;
    }

    public RawRangeXlyder sliderFunctionX(SliderFunction xSliderFunction) {
        this.assertMutable();
        this.xSliderFunction = xSliderFunction;
        return this;
    }

    public SliderFunction sliderFunctionX() {
        return this.xSliderFunction;
    }

    public RawRangeXlyder sliderFunctionY(SliderFunction ySliderFunction) {
        this.assertMutable();
        this.ySliderFunction = ySliderFunction;
        return this;
    }

    public SliderFunction sliderFunctionY() {
        return this.ySliderFunction;
    }

    public RawRangeXlyder incrementStep(@Nullable Double incrementStep) {
        this.assertMutable();
        this.xIncrementStep = incrementStep;
        this.yIncrementStep = incrementStep;
        return this;
    }

    public RawRangeXlyder incrementStep(double incrementStep) {
        this.assertMutable();
        this.xIncrementStep = incrementStep;
        this.yIncrementStep = incrementStep;
        return this;
    }

    public RawRangeXlyder incrementStepX(@Nullable Double xIncrementStep) {
        this.assertMutable();
        this.xIncrementStep = xIncrementStep;
        return this;
    }

    public RawRangeXlyder incrementStepX(double xIncrementStep) {
        this.assertMutable();
        this.xIncrementStep = xIncrementStep;
        return this;
    }

    public @Nullable Double incrementStepX() {
        return this.xIncrementStep;
    }

    public RawRangeXlyder incrementStepY(@Nullable Double yIncrementStep) {
        this.assertMutable();
        this.yIncrementStep = yIncrementStep;
        return this;
    }

    public RawRangeXlyder incrementStepY(double yIncrementStep) {
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

    public static class State extends WidgetState<RawRangeXlyder> {

        protected final Vector2d minDragValue = new Vector2d();
        protected final Vector2d maxDragValue = new Vector2d();
        protected @Nullable Handle grabbedHandle = null;
        protected boolean dragging = false;

        protected Vector2dc normalizedMinValue;
        protected Vector2dc normalizedMaxValue;
        protected Vector2dc xIncrementStep;
        protected Vector2dc yIncrementStep;

        @Override
        public void init() {
            var widget = this.widget();
            this.xIncrementStep = new Vector2d(
                widget.xIncrementStep != null ? widget.xSliderFunction.normalize(widget.xIncrementStep, widget.min.x, widget.max.x) : 0.01,
                widget.xIncrementStep != null ? widget.xSliderFunction.normalize(widget.xIncrementStep, widget.min.x, widget.max.x) : 0.01
            );
            this.yIncrementStep = new Vector2d(
                widget.yIncrementStep != null ? widget.ySliderFunction.normalize(widget.yIncrementStep, widget.min.y, widget.max.y) : 0.01,
                widget.yIncrementStep != null ? widget.ySliderFunction.normalize(widget.yIncrementStep, widget.min.y, widget.max.y) : 0.01
            );
        }

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            this.normalizedMinValue = new Vector2d(
                widget.xSliderFunction.normalize(widget.minValue.x(), widget.min.x, widget.max.x),
                widget.ySliderFunction.normalize(widget.minValue.y(), widget.min.y, widget.max.y)
            );
            this.normalizedMaxValue = new Vector2d(
                widget.xSliderFunction.normalize(widget.maxValue.x(), widget.min.x, widget.max.x),
                widget.ySliderFunction.normalize(widget.maxValue.y(), widget.min.y, widget.max.y)
            );

            return new LayoutBuilder((innerContext, constraints) -> {
                var content = new Stack(
                    Alignment.TOP_LEFT,
                    new Sized(constraints.maxWidth(), constraints.maxHeight(), widget.track),
                    // Min handle
                    new Padding(
                        Insets.left(Math.floor((constraints.maxWidth() - widget.minHandleSize.width()) * this.normalizedMinValue.x()))
                            .withTop(Math.floor((constraints.maxHeight() - widget.minHandleSize.height()) * (1 - this.normalizedMinValue.y()))),
                        new Sized(widget.minHandleSize, widget.minHandle)
                    ),
                    // Max handle
                    new Padding(
                        Insets.left(Math.floor((constraints.maxWidth() - widget.maxHandleSize.width()) * this.normalizedMaxValue.x()))
                            .withTop(Math.floor((constraints.maxHeight() - widget.maxHandleSize.height()) * (1 - this.normalizedMaxValue.y()))),
                        new Sized(widget.maxHandleSize, widget.maxHandle)
                    )
                );

                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Incrementor(
                            xIncrement -> {
                                if (this.grabbedHandle == Handle.MIN || this.grabbedHandle == null) {
                                    this.applyValue(
                                        MathHelper.clamp(this.normalizedMinValue.x() + this.xIncrementStep.x() * xIncrement, 0, 1),
                                        null, null, null
                                    );
                                } else {
                                    this.applyValue(
                                        null, null,
                                        MathHelper.clamp(this.normalizedMaxValue.x() + this.xIncrementStep.x() * xIncrement, 0, 1),
                                        null
                                    );
                                }
                            },
                            yIncrement -> {
                                if (this.grabbedHandle == Handle.MIN || this.grabbedHandle == null) {
                                    this.applyValue(
                                        null,
                                        MathHelper.clamp(this.normalizedMinValue.y() + this.yIncrementStep.y() * yIncrement, 0, 1),
                                        null, null
                                    );
                                } else {
                                    this.applyValue(
                                        null, null, null,
                                        MathHelper.clamp(this.normalizedMaxValue.y() + this.yIncrementStep.y() * yIncrement, 0, 1)
                                    );
                                }
                            },
                            new MouseArea(
                                mouseArea -> mouseArea
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;

                                        y = constraints.maxHeight() - y;
                                        this.grabbedHandle = this.handleAt(constraints, x, y);

                                        Vector2dc initialDragValue;
                                        if (this.grabbedHandle == Handle.MIN) {
                                            initialDragValue = new Vector2d(this.normalizedMinValue);
                                            if (!this.isInMinHandle(constraints, x, y)) {
                                                initialDragValue = this.setAbsolute(constraints, x, y, Handle.MIN);
                                            }
                                            this.minDragValue.set(initialDragValue);
                                        } else {
                                            initialDragValue = new Vector2d(this.normalizedMaxValue);
                                            if (!this.isInMaxHandle(constraints, x, y)) {
                                                initialDragValue = this.setAbsolute(constraints, x, y, Handle.MAX);
                                            }
                                            this.maxDragValue.set(initialDragValue);
                                        }

                                        this.dragging = true;
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> this.move(constraints, dx, -dy))
                                    .dragEndCallback(() -> this.dragging = false)
                                    .cursorStyleSupplier((x, y) -> {
                                        y = constraints.maxHeight() - y;
                                        if (!this.isInMinHandle(constraints, x, y) && !this.isInMaxHandle(constraints, x, y) && !this.dragging) {
                                            return CursorStyle.HAND;
                                        }
                                        return CursorStyle.MOVE;
                                    }),
                                content
                            )
                        )
                );
            });
        }

        protected Handle handleAt(Constraints constraints, double x, double y) {
            boolean inMin = this.isInMinHandle(constraints, x, y);
            boolean inMax = this.isInMaxHandle(constraints, x, y);

            if (inMin && inMax) {
                // If both handles overlap, choose the closest one
                var minCenterX = (constraints.maxWidth() - this.widget().minHandleSize.width()) * this.normalizedMinValue.x() + this.widget().minHandleSize.width() / 2;
                var minCenterY = (constraints.maxHeight() - this.widget().minHandleSize.height()) * this.normalizedMinValue.y() + this.widget().minHandleSize.height() / 2;
                var maxCenterX = (constraints.maxWidth() - this.widget().maxHandleSize.width()) * this.normalizedMaxValue.x() + this.widget().maxHandleSize.width() / 2;
                var maxCenterY = (constraints.maxHeight() - this.widget().maxHandleSize.height()) * this.normalizedMaxValue.y() + this.widget().maxHandleSize.height() / 2;

                var distToMin = Math.sqrt(Math.pow(x - minCenterX, 2) + Math.pow(y - minCenterY, 2));
                var distToMax = Math.sqrt(Math.pow(x - maxCenterX, 2) + Math.pow(y - maxCenterY, 2));

                return distToMin <= distToMax ? Handle.MIN : Handle.MAX;
            }
            if (inMin) return Handle.MIN;
            if (inMax) return Handle.MAX;

            // If not in any handle, choose the closest one
            var minCenterX = (constraints.maxWidth() - this.widget().minHandleSize.width()) * this.normalizedMinValue.x() + this.widget().minHandleSize.width() / 2;
            var minCenterY = (constraints.maxHeight() - this.widget().minHandleSize.height()) * this.normalizedMinValue.y() + this.widget().minHandleSize.height() / 2;
            var maxCenterX = (constraints.maxWidth() - this.widget().maxHandleSize.width()) * this.normalizedMaxValue.x() + this.widget().maxHandleSize.width() / 2;
            var maxCenterY = (constraints.maxHeight() - this.widget().maxHandleSize.height()) * this.normalizedMaxValue.y() + this.widget().maxHandleSize.height() / 2;

            var distToMin = Math.sqrt(Math.pow(x - minCenterX, 2) + Math.pow(y - minCenterY, 2));
            var distToMax = Math.sqrt(Math.pow(x - maxCenterX, 2) + Math.pow(y - maxCenterY, 2));

            return distToMin <= distToMax ? Handle.MIN : Handle.MAX;
        }

        protected boolean isInMinHandle(Constraints constraints, double x, double y) {
            var trackWidth = constraints.maxWidth() - this.widget().minHandleSize.width();
            var trackHeight = constraints.maxHeight() - this.widget().minHandleSize.height();

            var handleMinX = this.normalizedMinValue.x() * trackWidth;
            var handleMinY = this.normalizedMinValue.y() * trackHeight;
            var handleMaxX = handleMinX + this.widget().minHandleSize.width();
            var handleMaxY = handleMinY + this.widget().minHandleSize.height();

            return x >= handleMinX && x <= handleMaxX && y >= handleMinY && y <= handleMaxY;
        }

        protected boolean isInMaxHandle(Constraints constraints, double x, double y) {
            var trackWidth = constraints.maxWidth() - this.widget().maxHandleSize.width();
            var trackHeight = constraints.maxHeight() - this.widget().maxHandleSize.height();

            var handleMinX = this.normalizedMaxValue.x() * trackWidth;
            var handleMinY = this.normalizedMaxValue.y() * trackHeight;
            var handleMaxX = handleMinX + this.widget().maxHandleSize.width();
            var handleMaxY = handleMinY + this.widget().maxHandleSize.height();

            return x >= handleMinX && x <= handleMaxX && y >= handleMinY && y <= handleMaxY;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            if (this.widget().onChanged == null || this.grabbedHandle == null) return;

            var trackWidth = constraints.maxWidth() - (this.grabbedHandle == Handle.MIN ? this.widget().minHandleSize.width() : this.widget().maxHandleSize.width());
            var trackHeight = constraints.maxHeight() - (this.grabbedHandle == Handle.MIN ? this.widget().minHandleSize.height() : this.widget().maxHandleSize.height());

            var deltaNormX = dx / trackWidth;
            var deltaNormY = dy / trackHeight;

            if (this.grabbedHandle == Handle.MIN) {
                this.minDragValue.x += deltaNormX;
                this.minDragValue.y += deltaNormY;
                this.minDragValue.x = MathHelper.clamp(this.minDragValue.x, 0, 1);
                this.minDragValue.y = MathHelper.clamp(this.minDragValue.y, 0, 1);
                this.applyValue(this.minDragValue.x, this.minDragValue.y, null, null);
            } else {
                this.maxDragValue.x += deltaNormX;
                this.maxDragValue.y += deltaNormY;
                this.maxDragValue.x = MathHelper.clamp(this.maxDragValue.x, 0, 1);
                this.maxDragValue.y = MathHelper.clamp(this.maxDragValue.y, 0, 1);
                this.applyValue(null, null, this.maxDragValue.x, this.maxDragValue.y);
            }
        }

        protected Vector2dc setAbsolute(Constraints constraints, double x, double y, Handle handle) {
            if (this.widget().onChanged == null) {
                return handle == Handle.MIN ? this.normalizedMinValue : this.normalizedMaxValue;
            }

            var handleSize = handle == Handle.MIN ? this.widget().minHandleSize : this.widget().maxHandleSize;

            var newNormalizedX = MathHelper.clamp((x - (handleSize.width() / 2)) / (constraints.maxWidth() - handleSize.width()), 0, 1);
            var newNormalizedY = MathHelper.clamp((y - (handleSize.height() / 2)) / (constraints.maxHeight() - handleSize.height()), 0, 1);

            if (handle == Handle.MIN) {
                this.applyValue(newNormalizedX, newNormalizedY, null, null);
            } else {
                this.applyValue(null, null, newNormalizedX, newNormalizedY);
            }

            return new Vector2d(newNormalizedX, newNormalizedY);
        }

        protected void applyValue(@Nullable Double newMinNormalizedX, @Nullable Double newMinNormalizedY,
                                  @Nullable Double newMaxNormalizedX, @Nullable Double newMaxNormalizedY) {
            var widget = this.widget();

            double newMinX = widget.minValue.x();
            double newMinY = widget.minValue.y();
            double newMaxX = widget.maxValue.x();
            double newMaxY = widget.maxValue.y();

            if (newMinNormalizedX != null) {
                newMinX = widget.xSliderFunction.deNormalize(newMinNormalizedX, widget.min.x, widget.max.x);
                if (widget.xStep != null) {
                    newMinX = Math.round(newMinX / widget.xStep) * widget.xStep;
                }
                newMinX = MathHelper.clamp(newMinX, widget.min.x, widget.max.x);
            }

            if (newMinNormalizedY != null) {
                newMinY = widget.ySliderFunction.deNormalize(newMinNormalizedY, widget.min.y, widget.max.y);
                if (widget.yStep != null) {
                    newMinY = Math.round(newMinY / widget.yStep) * widget.yStep;
                }
                newMinY = MathHelper.clamp(newMinY, widget.min.y, widget.max.y);
            }

            if (newMaxNormalizedX != null) {
                newMaxX = widget.xSliderFunction.deNormalize(newMaxNormalizedX, widget.min.x, widget.max.x);
                if (widget.xStep != null) {
                    newMaxX = Math.round(newMaxX / widget.xStep) * widget.xStep;
                }
                newMaxX = MathHelper.clamp(newMaxX, widget.min.x, widget.max.x);
            }

            if (newMaxNormalizedY != null) {
                newMaxY = widget.ySliderFunction.deNormalize(newMaxNormalizedY, widget.min.y, widget.max.y);
                if (widget.yStep != null) {
                    newMaxY = Math.round(newMaxY / widget.yStep) * widget.yStep;
                }
                newMaxY = MathHelper.clamp(newMaxY, widget.min.y, widget.max.y);
            }

            widget.onChanged.accept(newMinX, newMinY, newMaxX, newMaxY);
        }

        protected enum Handle {
            MIN, MAX
        }
    }
}
