package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

public class Xlyder extends StatelessWidget {

    public final double xValue, yValue;
    public final double minX, minY;
    public final double maxX, maxY;
    public final @Nullable Double xStep, yStep;

    public final RawXlyder.XlyderCallback onChanged;

    public Xlyder(
        double xValue, double yValue,
        double minX, double minY,
        double maxX, double maxY,
        @Nullable Double step, @Nullable Double yStep,
        RawXlyder.XlyderCallback onChanged
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
    }

    public Xlyder(double xValue, double yValue, RawXlyder.XlyderCallback onChanged) {
        this(xValue, yValue, 0, 0, 1, 1, null, null, onChanged);
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawXlyder(
            this.xValue, this.yValue,
            this.minX, this.minY,
            this.maxX, this.maxY,
            this.xStep, this.yStep,
            this.onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            new DefaultSliderHandle(),
            Size.of(8, 8)
        );
    }
}
