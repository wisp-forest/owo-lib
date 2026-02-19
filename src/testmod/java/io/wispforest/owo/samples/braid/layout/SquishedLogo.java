package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidLogo;
import io.wispforest.owo.braid.widgets.basic.Sized;

public class SquishedLogo extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        // squish the braid logo on the vertical axis
        return new Sized(
            null,
            32, // the braid logo is normally 64x64, so this
            // makes it half that size vertically
            new BraidLogo()
        );
    }
}
