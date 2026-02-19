package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidLogo;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Padding;

public class PaddedLogo extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Box(
            Color.WHITE,             // white backdrop to visualize the padding
            new Padding(
                Insets.all(5), // insert 5 pixels of padding on all sides
                new Box(
                    Color.BLACK,     // black background to visualize the widget
                    new BraidLogo()  // bounds
                )
            )
        );
    }
}
