package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidLogo;
import io.wispforest.owo.braid.widgets.basic.Align;

public class BottomRightLogo extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        // move the logo to the bottom right of the
        // available space
        return new Align(
            Alignment.BOTTOM_RIGHT,
            new BraidLogo()
        );
    }
}
