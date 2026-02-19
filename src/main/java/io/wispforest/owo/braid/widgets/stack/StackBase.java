package io.wispforest.owo.braid.widgets.stack;

import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;

public class StackBase extends VisitorWidget {

    public StackBase(Widget child) {
        super(child);
    }

    private static final Visitor<StackBase> VISITOR = (widget, instance) -> {
        if (instance.parentData != StackParentData.INSTANCE) {
            instance.parentData = StackParentData.INSTANCE;
            instance.markNeedsLayout();
        }
    };

    @Override
    public Proxy<?> proxy() {
        return new VisitorWidget.Proxy<>(this, VISITOR);
    }
}
