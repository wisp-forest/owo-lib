package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Flex;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.label.Label;

public class VerticalFlex extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Sized(                   // since our example environment has loose constraints
                                            // and we want to see a long main axis, force it
                                            // to have the maximum possible size
            null, Double.POSITIVE_INFINITY,
            new Flex(
                LayoutAxis.VERTICAL,        // main axis is vertical
                MainAxisAlignment.END,      // align to the end of the main axis, i.e. the bottom
                CrossAxisAlignment.STRETCH, // stretch the cross axis to its maximum possible size
                                            // and force the children to fill it
                Label.literal("child 1"),
                Label.literal("child 2")
            )
        );
    }
}
