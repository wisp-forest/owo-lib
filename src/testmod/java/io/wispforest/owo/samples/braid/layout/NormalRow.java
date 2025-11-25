package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;

public class NormalRow extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Row(                     // main axis is always horizontal in a row
                                            // by not specifying alignment for either axis,
                                            // we default to start alignment on both
            Label.literal("child 1"),
            Label.literal("child 2")
        );
    }
}
