package io.wispforest.owo.braid.widgets.slider.xlyder;

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
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.braid.widgets.slider.Incrementor;
import io.wispforest.owo.braid.widgets.slider.SliderStyle;
import io.wispforest.owo.braid.widgets.slider.slider.SliderFunction;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.Objects;

public class Xlyder extends StatefulWidget {

    protected final Vector2d min = new Vector2d();
    protected final Vector2d max = new Vector2d(1);
    protected @Nullable Double xStep;
    protected @Nullable Double yStep;
    protected SliderFunction xSliderFunction = SliderFunction.LINEAR;
    protected SliderFunction ySliderFunction = SliderFunction.LINEAR;
    protected @Nullable Double xIncrementStep = null;
    protected @Nullable Double yIncrementStep = null;
    protected SliderStyle<Size> style;

    public final Vector2dc value;
    public final @Nullable XlyderCallback onChanged;

    public Xlyder(
        Vector2dc value,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        @Nullable XlyderCallback onChanged
    ) {
        this.value = value;
        this.onChanged = onChanged;
        if (setupCallback != null) setupCallback.setup(this);
    }

    public Xlyder(
        Vector2dc value,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged
    ) {
        this(value, setupCallback, active ? onChanged : null);
    }

    public Xlyder(
        double x, double y,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        @Nullable XlyderCallback onChanged
    ) {
        this(new Vector2d(x, y), setupCallback, onChanged);
    }

    public Xlyder(
        double x, double y,
        @Nullable WidgetSetupCallback<Xlyder> setupCallback,
        boolean active,
        XlyderCallback onChanged
    ) {
        this(new Vector2d(x, y), setupCallback, active ? onChanged : null);
    }

    public Xlyder min(Vector2d min) {
        this.assertMutable();
        this.min.set(min);
        return this;
    }

    public Xlyder min(double minX, double minY) {
        this.assertMutable();
        this.min.set(minX, minY);
        return this;
    }

    public Xlyder min(double min) {
        this.assertMutable();
        this.min.set(min, min);
        return this;
    }

    public Vector2dc min() {
        return this.min;
    }

    public Xlyder minX(double minX) {
        this.assertMutable();
        this.min.x = minX;
        return this;
    }

    public double minX() {
        return this.min.x;
    }

    public Xlyder minY(double minY) {
        this.assertMutable();
        this.min.y = minY;
        return this;
    }

    public double minY() {
        return this.min.y;
    }

    public Xlyder max(Vector2d max) {
        this.assertMutable();
        this.max.set(max);
        return this;
    }

    public Xlyder max(double maxX, double maxY) {
        this.assertMutable();
        this.max.set(maxX, maxY);
        return this;
    }

    public Xlyder max(double max) {
        this.assertMutable();
        this.max.set(max, max);
        return this;
    }

    public Vector2dc max() {
        return this.max;
    }

    public Xlyder maxX(double maxX) {
        this.assertMutable();
        this.max.x = maxX;
        return this;
    }

    public double maxX() {
        return this.max.x;
    }

    public Xlyder maxY(double maxY) {
        this.assertMutable();
        this.max.y = maxY;
        return this;
    }

    public double maxY() {
        return this.max.y;
    }

    public Xlyder range(Vector2d min, Vector2d max) {
        this.assertMutable();
        this.min.set(min);
        this.max.set(max);
        return this;
    }

    public Xlyder range(double minX, double minY, double maxX, double maxY) {
        this.assertMutable();
        this.min.set(minX, minY);
        this.max.set(maxX, maxY);
        return this;
    }

    public Xlyder range(double min, double max) {
        this.assertMutable();
        this.min.set(min, min);
        this.max.set(max, max);
        return this;
    }

    public Xlyder rangeX(double minX, double maxX) {
        this.assertMutable();
        this.min.x = minX;
        this.max.x = maxX;
        return this;
    }

    public Xlyder rangeY(double minY, double maxY) {
        this.assertMutable();
        this.min.y = minY;
        this.max.y = maxY;
        return this;
    }

    public Xlyder step(@Nullable Double step) {
        this.assertMutable();
        this.xStep = step;
        this.yStep = step;
        return this;
    }

    public Xlyder step(double step) {
        this.assertMutable();
        this.xStep = step;
        this.yStep = step;
        return this;
    }

    public Xlyder stepX(@Nullable Double xStep) {
        this.assertMutable();
        this.xStep = xStep;
        return this;
    }

    public Xlyder stepX(double xStep) {
        this.assertMutable();
        this.xStep = xStep;
        return this;
    }

    public @Nullable Double stepX() {
        return this.xStep;
    }

    public Xlyder stepY(@Nullable Double yStep) {
        this.assertMutable();
        this.yStep = yStep;
        return this;
    }

    public Xlyder stepY(double yStep) {
        this.assertMutable();
        this.yStep = yStep;
        return this;
    }

    public @Nullable Double stepY() {
        return this.yStep;
    }

    public Xlyder sliderFunction(SliderFunction sliderFunction) {
        this.assertMutable();
        this.xSliderFunction = sliderFunction;
        this.ySliderFunction = sliderFunction;
        return this;
    }

    public Xlyder sliderFunctionX(SliderFunction xSliderFunction) {
        this.assertMutable();
        this.xSliderFunction = xSliderFunction;
        return this;
    }

    public SliderFunction sliderFunctionX() {
        return this.xSliderFunction;
    }

    public Xlyder sliderFunctionY(SliderFunction ySliderFunction) {
        this.assertMutable();
        this.ySliderFunction = ySliderFunction;
        return this;
    }

    public SliderFunction sliderFunctionY() {
        return this.ySliderFunction;
    }

    public Xlyder incrementStep(@Nullable Double incrementStep) {
        this.assertMutable();
        this.xIncrementStep = incrementStep;
        this.yIncrementStep = incrementStep;
        return this;
    }

    public Xlyder incrementStep(double incrementStep) {
        this.assertMutable();
        this.xIncrementStep = incrementStep;
        this.yIncrementStep = incrementStep;
        return this;
    }

    public Xlyder incrementStepX(@Nullable Double xIncrementStep) {
        this.assertMutable();
        this.xIncrementStep = xIncrementStep;
        return this;
    }

    public Xlyder incrementStepX(double xIncrementStep) {
        this.assertMutable();
        this.xIncrementStep = xIncrementStep;
        return this;
    }

    public @Nullable Double incrementStepX() {
        return this.xIncrementStep;
    }

    public Xlyder incrementStepY(@Nullable Double yIncrementStep) {
        this.assertMutable();
        this.yIncrementStep = yIncrementStep;
        return this;
    }

    public Xlyder incrementStepY(double yIncrementStep) {
        this.assertMutable();
        this.yIncrementStep = yIncrementStep;
        return this;
    }

    public @Nullable Double incrementStepY() {
        return this.yIncrementStep;
    }

    public Xlyder style(SliderStyle<Size> style) {
        this.assertMutable();
        this.style = style;
        return this;
    }

    public @Nullable SliderStyle<Size> style() {
        return this.style;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<Xlyder> {

        protected final Vector2d dragValue = new Vector2d();
        protected boolean dragging = false;

        protected Vector2dc normalizedValue;
        protected Vector2dc incrementStep;

        protected Size handleSize;

        @Override
        public void init() {
            var widget = this.widget();
            var trueMinX = Math.min(widget.min.x, widget.max.x);
            var trueMaxX = Math.max(widget.min.x, widget.max.x);
            var trueMinY = Math.min(widget.min.y, widget.max.y);
            var trueMaxY = Math.max(widget.min.y, widget.max.y);
            this.incrementStep = new Vector2d(
                widget.xIncrementStep != null ? widget.xSliderFunction.normalize(widget.xIncrementStep, trueMinX, trueMaxX) : widget.xStep != null ? widget.xSliderFunction.normalize(widget.xStep, trueMinX, trueMaxX) : 0.01,
                widget.yIncrementStep != null ? widget.ySliderFunction.normalize(widget.yIncrementStep, trueMinY, trueMaxY) : widget.yStep != null ? widget.ySliderFunction.normalize(widget.yStep, trueMinY, trueMaxY) : 0.01
            );
        }

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            var effectiveStyle = widget.style != null ? widget.style : SliderStyle.<Size>getDefault();
            if (DefaultXlyderStyle.maybeOf(context) instanceof SliderStyle<Size> contextStyle) {
                effectiveStyle = effectiveStyle.overriding(contextStyle);
            }

            var disabled = widget.onChanged == null || ControlsOverride.controlsDisabled(context);

            var track = Objects.requireNonNullElse(effectiveStyle.track(), DEFAULT_TRACK);
            var handle = Objects.requireNonNullElse(effectiveStyle.handleBuilder(), DEFAULT_HANDLE_BUILDER).build(!disabled);
            this.handleSize = Objects.requireNonNullElse(effectiveStyle.handleSize(), DEFAULT_HANDLE_SIZE);
            //noinspection OptionalAssignedToNull
            var confirmSound = effectiveStyle.confirmSound() != null ? effectiveStyle.confirmSound().orElse(null) : SoundEvents.UI_BUTTON_CLICK.value();

            this.normalizedValue = new Vector2d(
                widget.xSliderFunction.normalize(widget.value.x(), widget.min.x, widget.max.x),
                widget.ySliderFunction.normalize(widget.value.y(), widget.min.y, widget.max.y)
            );
            return new LayoutBuilder((innerContext, constraints) -> {
                var content = new Stack(
                    Alignment.TOP_LEFT,
                    new Sized(constraints.maxWidth(), constraints.maxHeight(), track),
                    new Padding(
                        Insets.left(Math.floor((constraints.maxWidth() - this.handleSize.width()) * this.normalizedValue.x()))
                            .withTop(Math.floor((constraints.maxHeight() - this.handleSize.height()) * (1 - this.normalizedValue.y()))),
                        new Sized(this.handleSize, handle)
                    )
                );
                return new Center(
                    widget.onChanged == null || ControlsOverride.controlsDisabled(context)
                        ? content
                        : new Incrementor(
                            xIncrement -> this.applyValue(Mth.clamp(this.normalizedValue.x() + this.incrementStep.x() * xIncrement, 0, 1), null),
                            yIncrement -> this.applyValue(null, Mth.clamp(this.normalizedValue.y() + this.incrementStep.y() * yIncrement, 0, 1)),
                            new MouseArea(
                                mouseArea -> mouseArea
                                    //TODO: decide what to do with buttons here
                                    .clickCallback((x, y, button, modifiers) -> {
                                        if (button != 0) return false;

                                        y = constraints.maxHeight() - y;
                                        Vector2dc initialDragValue = new Vector2d(this.normalizedValue);
                                        if (!this.isInHandle(constraints, x, y)) initialDragValue = this.setAbsolute(constraints, x, y);

                                        this.dragValue.set(initialDragValue);
                                        this.dragging = true;
                                        return true;
                                    })
                                    .dragCallback((x, y, dx, dy) -> this.move(constraints, dx, -dy))
                                    .dragEndCallback(() -> {
                                        this.dragging = false;
                                        if (confirmSound != null) {
                                            UISounds.play(confirmSound);
                                        }
                                    })
                                    //TODO: invert the y passed here cuz it cringe atm
                                    .cursorStyleSupplier((x, y) -> (!this.isInHandle(constraints, x, constraints.maxHeight() - y) && !this.dragging) ? CursorStyle.HAND : CursorStyle.MOVE),
                                content
                            )
                        )
                );
            });
        }

        protected boolean isInHandle(Constraints constraints, double x, double y) {
            var trackWidth = constraints.maxWidth() - this.handleSize.width();
            var trackHeight = constraints.maxHeight() - this.handleSize.height();

            var handleMinX = this.normalizedValue.x() * trackWidth;
            var handleMinY = this.normalizedValue.y() * trackHeight;
            var handleMaxX = handleMinX + this.handleSize.width();
            var handleMaxY = handleMinY + this.handleSize.height();

            return x >= handleMinX && x <= handleMaxX && y >= handleMinY && y <= handleMaxY;
        }

        protected void move(Constraints constraints, double dx, double dy) {
            this.dragValue.add(
                dx / (constraints.maxWidth() - this.handleSize.width()),
                dy / (constraints.maxHeight() - this.handleSize.height())
            );

            this.applyValue(
                Mth.clamp(this.dragValue.x, 0, 1),
                Mth.clamp(this.dragValue.y, 0, 1)
            );
        }

        protected Vector2dc setAbsolute(Constraints constraints, double x, double y) {
            if (this.widget().onChanged == null) return this.normalizedValue;

            var handleSize = this.handleSize;

            var newNormalizedX = Mth.clamp((x - (handleSize.width() / 2)) / (constraints.maxWidth() - handleSize.width()), 0, 1);
            var newNormalizedY = Mth.clamp((y - (handleSize.height() / 2)) / (constraints.maxHeight() - handleSize.height()), 0, 1);

            this.applyValue(newNormalizedX, newNormalizedY);
            return new Vector2d(newNormalizedX, newNormalizedY);
        }

        protected void applyValue(@Nullable Double newNormalizedX, @Nullable Double newNormalizedY) {
            if (newNormalizedX == null && newNormalizedY == null) return;
            var widget = this.widget();
            double newX = widget.value.x();
            double newY = widget.value.y();
            if (newNormalizedX != null) newX = widget.xSliderFunction.deNormalize(newNormalizedX, widget.min.x, widget.max.x);
            if (newNormalizedY != null) newY = widget.ySliderFunction.deNormalize(newNormalizedY, widget.min.y, widget.max.y);
            widget.onChanged.accept(
                widget.xStep != null ? Math.round(newX / widget.xStep) * widget.xStep : newX,
                widget.yStep != null ? Math.round(newY / widget.yStep) * widget.yStep : newY
            );
        }
    }

    // ---

    private static final Widget DEFAULT_TRACK = new Panel(ButtonComponent.DISABLED_TEXTURE);
    private static final SliderStyle.HandleBuilder DEFAULT_HANDLE_BUILDER = DefaultSliderHandle::new;
    private static final Size DEFAULT_HANDLE_SIZE = Size.square(8.0);
}
