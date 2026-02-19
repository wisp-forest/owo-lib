package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidLogo;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Box;

public class SizeFactorLogo extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Box(
            Color.WHITE,            // white backdrop to visualize align size
            new Align(
                Alignment.TOP_LEFT,
                1.5,
                1.5,
                new Box(
                    Color.BLACK,    // black backdrop to visualize widget bounds
                    new BraidLogo()
                )
            )
        );
    }
}
