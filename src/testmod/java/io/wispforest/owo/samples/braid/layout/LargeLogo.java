package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidLogo;
import io.wispforest.owo.braid.widgets.basic.Constrain;

public class LargeLogo extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Constrain(
            Constraints.only(
                128.0, // minimum width
                128.0, // minimum height
                null, // no maximum width
                null // no maximum height
            ),
            new BraidLogo()
        );
    }
}
