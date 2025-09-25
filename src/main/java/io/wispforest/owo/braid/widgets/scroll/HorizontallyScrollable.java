package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class HorizontallyScrollable extends Scrollable {
    public HorizontallyScrollable(@Nullable ScrollController controller, @Nullable ScrollAnimationSettings animationSettings, Widget child) {
        super(true, false, controller, null, animationSettings, child);
    }

    public HorizontallyScrollable(Widget child) {
        this(null, null, child);
    }
}
