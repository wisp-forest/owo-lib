package io.wispforest.uwu.client.braid;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.grid.Grid;

public class Amogus extends StatelessWidget {

    public final Widget bodyPixel;
    public final Widget visorPixel;
    public final double pixelSize;

    public Amogus(Widget bodyPixel, Widget visorPixel, double pixelSize) {
        this.bodyPixel = bodyPixel;
        this.visorPixel = visorPixel;
        this.pixelSize = pixelSize;
    }

    @Override
    public Widget build(BuildContext context) {
        return new Sized(
            this.pixelSize * 4,
            this.pixelSize * 4,
            new Grid(
                LayoutAxis.VERTICAL,
                4,
                Grid.CellFit.tight(),
                (Widget) null,
                this.bodyPixel,
                this.bodyPixel,
                this.bodyPixel,
                this.bodyPixel,
                this.bodyPixel,
                this.visorPixel,
                this.visorPixel,
                this.bodyPixel,
                this.bodyPixel,
                this.bodyPixel,
                this.bodyPixel,
                null,
                this.bodyPixel,
                null,
                this.bodyPixel
            )
        );
    }
}
