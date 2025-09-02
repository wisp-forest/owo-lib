package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class VerticallyScrollable extends Scrollable {
    public VerticallyScrollable(@Nullable ScrollController controller, @Nullable ScrollAnimationSettings animationSettings, Widget child) {
        super(false, true, null, controller, animationSettings, child);
    }

    public VerticallyScrollable(Widget child) {
        this(null, null, child);
    }
}
