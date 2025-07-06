package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class MessageXlyder extends StatelessWidget {

    public final double xValue, yValue;
    public final double minX, minY;
    public final double maxX, maxY;
    public final @Nullable Double xStep, yStep;

    public final RawXlyder.XlyderCallback onChanged;
    public final Text message;

    public MessageXlyder(
        double xValue, double yValue,
        double minX, double minY,
        double maxX, double maxY,
        @Nullable Double step, @Nullable Double yStep,
        RawXlyder.XlyderCallback onChanged,
        Text message
    ) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.xStep = step;
        this.yStep = yStep;

        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageXlyder(double xValue, double yValue, RawXlyder.XlyderCallback onChanged, Text message) {
        this(xValue, yValue, 0, 0, 1, 1, null, null, onChanged, message);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Xlyder(
                this.xValue, this.yValue,
                this.minX, this.minY,
                this.maxX, this.maxY,
                this.xStep, this.yStep,
                this.onChanged
            ),
            new Label(
                LabelStyle.SHADOW,
                false,
                this.message
            )
        );
    }

    @FunctionalInterface
    public interface XlyderMessageProvider {
        Text getMessage(double x, double y);
    }
}
