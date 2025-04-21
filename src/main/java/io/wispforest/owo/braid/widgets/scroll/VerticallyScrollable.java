package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class VerticallyScrollable extends Scrollable {
    public VerticallyScrollable(@Nullable ScrollController controller, Widget child) {
        super(false, true, null, controller, child);
    }

    public VerticallyScrollable(Widget child) {
        this(null, child);
    }
}
