package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.object.entity.EntityDisplayMode;
import io.wispforest.owo.braid.widgets.object.entity.EntityWidget;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.uwu.client.braid.BurningChyz;

import java.util.stream.Stream;

public class OptimizationTest extends StatelessWidget {

    @Override
    public Widget build(BuildContext context) {
        var widget = new Sized(
            128, 128,
            new EntityWidget(
                1.5, BurningChyz.of(context),
                entityWidget -> entityWidget.displayMode(EntityDisplayMode.CURSOR)
            )
        );
        return new VerticallyScrollable(
            new Grid(
                LayoutAxis.VERTICAL,
                32,
                Grid.CellFit.loose(),
                Stream.generate(() -> widget).limit(32 * 32).toList()
            )
        );
    }
}
