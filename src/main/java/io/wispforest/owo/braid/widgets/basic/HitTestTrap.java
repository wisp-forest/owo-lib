package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.Widget;

public class HitTestTrap extends VisitorWidget {
    public HitTestTrap(Widget child) {
        super(child);
    }

    public static final Visitor<HitTestTrap> VISITOR = (widget, instance) -> {
        instance.flags |= WidgetInstance.FLAG_HIT_TEST_BOUNDARY;
    };

    @Override
    public Proxy<?> proxy() {
        return new Proxy<>(this, VISITOR);
    }
}
