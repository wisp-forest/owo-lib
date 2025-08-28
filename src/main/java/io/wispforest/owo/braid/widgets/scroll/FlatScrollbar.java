package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.HoverableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.ui.core.Color;

public class FlatScrollbar extends Scrollbar {
    public FlatScrollbar(LayoutAxis axis, ScrollController controller, Color color, Color hoveredColor) {
        super(
            axis,
            controller,
            new Padding(Insets.none()),
            new HoverableBuilder(
                new Box(color),
                new Box(hoveredColor)
            )
        );
    }
}
