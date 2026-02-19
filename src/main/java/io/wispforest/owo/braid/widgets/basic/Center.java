package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class Center extends Align {
    public Center(@Nullable Double widthFactor, @Nullable Double heightFactor, Widget child) {
        super(Alignment.CENTER, widthFactor, heightFactor, child);
    }

    public Center(Widget child) {
        this(null, null, child);
    }
}
