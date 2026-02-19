package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.stack.Stack;

public class RGBStack extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Center(
            new Stack(
                Alignment.BOTTOM_RIGHT,
                new Sized(
                    Size.square(60),
                    new Box(Color.RED)
                ),
                new Sized(
                    Size.square(40),
                    new Box(Color.GREEN)
                ),
                new Sized(
                    Size.square(20),
                    new Box(Color.BLUE)
                )
            )
        );
    }
}
