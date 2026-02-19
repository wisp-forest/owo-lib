package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.label.Label;

import java.util.List;

public class PaddedColumn extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Column(                     // main axis is always vertical in a row
            MainAxisAlignment.CENTER,          // by setting the alignment to center on both axis we'll
            CrossAxisAlignment.CENTER,         // achieve the same thing a Center widget would

            new Padding(Insets.all(2)),  // we specify a separator (just padding in this case) to
                                               // space the children out nicely
            List.of(
                Label.literal("child 1"), // when using a separator, we must specify the children
                Label.literal("child 2")  // in a List to differentiate the method signature and
            )                                  // tell braid which widget should be the separator
        );
    }
}
