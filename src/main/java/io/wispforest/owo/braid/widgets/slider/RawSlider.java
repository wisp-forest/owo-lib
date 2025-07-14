package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class RawSlider extends StatelessWidget {

    public final double value;
    public final double min;
    public final double max;
    public final @Nullable Double step;
    public final LayoutAxis axis;

    public final DoubleConsumer onChanged;
    public final Widget track;
    public final Widget handle;
    public final double handleSize;

    public RawSlider(
        double value,
        double min,
        double max,
        @Nullable Double step,
        LayoutAxis axis,
        DoubleConsumer onChanged,
        Widget track,
        Widget handle,
        double handleSize
    ) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.axis = axis;
        this.onChanged = onChanged;
        this.track = track;
        this.handle = handle;
        this.handleSize = handleSize;
    }

    @Override
    public Widget build(BuildContext context) {
        return new LayoutBuilder((innerContext, constraints) -> {
            var normalizedValue = (this.discretize(this.value) - this.min) / (this.max - this.min);

            return new Center(
                new MouseArea(
                    widget -> widget
                        //TODO: decide what to do with buttons here
                        .clickCallback((x, y, button) -> {
                            if (button != 0) return false;
                            this.updateForMousePosition(constraints, x, y);
                            return true;
                        })
                        .dragCallback((x, y, dx, dy) -> this.updateForMousePosition(constraints, x, y))
                        .cursorStyle(CursorStyle.HAND),
                    new Stack(
                        this.axis.choose(Alignment.LEFT, Alignment.TOP),
                        new Sized(
                            constraints.maxWidth(),
                            constraints.maxHeight(),
                            this.track
                        ),
                        new Padding(
                            this.axis.chooseCompute(
                                () -> Insets.left(Math.floor((constraints.maxWidth() - this.handleSize) * normalizedValue)),
                                () -> Insets.top(Math.floor((constraints.maxHeight() - this.handleSize) * (1 - normalizedValue)))
                            ),
                            this.axis.chooseCompute(
                                () -> new Sized(
                                    this.handleSize,
                                    constraints.maxHeight(),
                                    this.handle
                                ),
                                () -> new Sized(
                                    constraints.maxWidth(),
                                    this.handleSize,
                                    this.handle
                                )
                            )
                        )
                    )
                )
            );
        });
    }

    private void updateForMousePosition(Constraints constraints, double x, double y) {
        var newNormalizedValue = MathHelper.clamp((this.axis.choose(x, y) - (this.handleSize / 2)) / (constraints.maxOnAxis(this.axis) - this.handleSize), 0, 1);
        if (this.axis == LayoutAxis.VERTICAL) newNormalizedValue = 1 - newNormalizedValue;

        this.onChanged.accept(this.discretize(this.min + newNormalizedValue * (this.max - this.min)));
    }

    private double discretize(double value) {
        if (this.step == null) return value;
        return Math.round(value / this.step) * this.step;
    }
}
