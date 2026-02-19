package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.Widget;

public class HitTestTrap extends VisitorWidget {

    public final boolean occludeHitTest;

    public HitTestTrap(boolean occludeHitTest, Widget child) {
        super(child);
        this.occludeHitTest = occludeHitTest;
    }

    public HitTestTrap(Widget child) {
        this(true, child);
    }

    public static final Visitor<HitTestTrap> VISITOR = (widget, instance) -> {
        if (widget.occludeHitTest) {
            instance.flags |= WidgetInstance.FLAG_HIT_TEST_BOUNDARY;
        } else {
            instance.flags &= ~WidgetInstance.FLAG_HIT_TEST_BOUNDARY;
        }
    };

    @Override
    public Proxy<?> proxy() {
        return new Proxy<>(this, VISITOR);
    }
}
