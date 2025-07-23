package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class RawXlyder extends StatelessWidget {

    public final double xValue, yValue;
    public final double minX, minY;
    public final double maxX, maxY;
    public final @Nullable Double xStep, yStep;

    public final XlyderCallback onChanged;
    public final Widget track;
    public final Widget handle;
    public final Size handleSize;

    public RawXlyder(
        double xValue, double yValue,
        double minX, double minY,
        double maxX, double maxY,
        @Nullable Double xStep, @Nullable Double yStep,
        XlyderCallback onChanged,
        Widget track,
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

        this.onChanged = onChanged;
        this.track = track;
        this.handle = handle;
        this.handleSize = handleSize;
    }

    @Override
    public Widget build(BuildContext context) {
        return new LayoutBuilder((innerContext, constraints) -> {
            var normalizedX = (this.discretize(this.xValue, this.xStep) - this.minX) / (this.maxX - this.minX);
            var normalizedY = (this.discretize(this.yValue, this.yStep) - this.minY) / (this.maxY - this.minY);

            return new Center(
                new MouseArea(
                    widget -> widget
                        //TODO: decide what to do with buttons here
                        .clickCallback((x, y, button, modifiers) -> {
                            if (button != 0) return false;
                            this.updateForMousePosition(constraints, x, y);
                            return true;
                        })
                        .dragCallback((x, y, dx, dy) -> this.updateForMousePosition(constraints, x, y))
                        .cursorStyle(CursorStyle.HAND),
                    new Stack(
                        Alignment.TOP_LEFT,
                        new Sized(
                            constraints.maxWidth(),
                            constraints.maxHeight(),
                            this.track
                        ),
                        new Padding(
                            Insets.left(Math.floor((constraints.maxWidth() - this.handleSize.width()) * normalizedX))
                                .withTop(Math.floor((constraints.maxHeight() - this.handleSize.height()) * (1 - normalizedY))),
                            new Sized(
                                this.handleSize,
                                this.handle
                            )
                        )
                    )
                )
            );
        });
    }

    private void updateForMousePosition(Constraints constraints, double x, double y) {
        var normalizedX = MathHelper.clamp((x - (this.handleSize.width() / 2)) / (constraints.maxWidth() - this.handleSize.width()), 0, 1);
        var normalizedY = MathHelper.clamp(1 - (y - (this.handleSize.height() / 2)) / (constraints.maxHeight() - this.handleSize.height()), 0, 1);
        this.onChanged.accept(
            this.discretize(this.minX + normalizedX * (this.maxX - this.minX), this.xStep),
            this.discretize(this.minY + normalizedY * (this.maxY - this.minY), this.yStep)
        );
    }

    private double discretize(double value, @Nullable Double step) {
        if (step == null) return value;
        return Math.round(value / step) * step;
    }

    @FunctionalInterface
    public interface XlyderCallback {
        void accept(double x, double y);
    }
}
