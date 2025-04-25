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

public class RawRangeSlider extends StatelessWidget {

    public final double minValue, maxValue;
    public final double min;
    public final double max;
    public final @Nullable Double step;
    public final LayoutAxis axis;

    public final RangeSliderCallback onChanged;
    public final Widget track;
    public final Widget handle;
    public final double handleSize;

    public RawRangeSlider(
        double minValue,
        double maxValue,
        double min,
        double max,
        @Nullable Double step,
        LayoutAxis axis,
        RangeSliderCallback onChanged,
        Widget track,
        Widget handle,
        double handleSize
    ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
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
            var normalizedMin = (this.discretize(this.minValue) - this.min) / (this.max - this.min);
            var normalizedMax = (this.discretize(this.maxValue) - this.min) / (this.max - this.min);

            return new Center(
                new MouseArea(
                    widget -> widget
                        .clickCallback((x, y) -> this.updateForMousePosition(constraints, x, y))
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
                                () -> Insets.left(Math.floor((constraints.maxWidth() - this.handleSize) * normalizedMin)),
                                () -> Insets.top(Math.floor((constraints.maxHeight() - this.handleSize) * normalizedMin))
                            ),
                            new Sized(
                                this.handleSize,
                                constraints.maxOnAxis(this.axis.opposite()),
                                this.handle
                            )
                        ),
                        new Padding(
                            this.axis.chooseCompute(
                                () -> Insets.left(Math.floor((constraints.maxWidth() - this.handleSize) * normalizedMax)),
                                () -> Insets.top(Math.floor((constraints.maxHeight() - this.handleSize) * normalizedMax))
                            ),
                            new Sized(
                                this.handleSize,
                                constraints.maxOnAxis(this.axis.opposite()),
                                this.handle
                            )
                        )
                    )
                )
            );
        });
    }

    private void updateForMousePosition(Constraints constraints, double x, double y) {
        var normalized = MathHelper.clamp((this.axis.choose(x, y) - (this.handleSize / 2)) / (constraints.maxOnAxis(this.axis) - this.handleSize), 0, 1);
        var distanceToMin = Math.abs(normalized - this.minValue);
        var distanceToMax = Math.abs(normalized - this.maxValue);
        this.onChanged.accept(
            distanceToMin < distanceToMax || (distanceToMin == distanceToMax && normalized < this.minValue) ? this.discretize(this.min + normalized * (this.max - this.min)) : this.minValue,
            distanceToMax < distanceToMin || (distanceToMin == distanceToMax && normalized > this.maxValue) ? this.discretize(this.min + normalized * (this.max - this.min)) : this.maxValue
        );
    }

    private double discretize(double value) {
        if (this.step == null) return value;
        return Math.round(value / this.step) * this.step;
    }

    @FunctionalInterface
    public interface RangeSliderCallback {
        void accept(double min, double max);
    }
}
