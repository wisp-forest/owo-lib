package io.wispforest.owo.braid.widgets.drag;

import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;

public class DragArenaElement extends VisitorWidget {
    public final double x, y;

    public DragArenaElement(double x, double y, Widget child) {
        super(child);
        this.x = x;
        this.y = y;
    }

    public static final Visitor<DragArenaElement> VISITOR = (widget, instance) -> {
        if (instance.parentData instanceof DragParentData data) {
            data.x = widget.x;
            data.y = widget.y;
        } else {
            instance.parentData = new DragParentData(widget.x, widget.y);
        }

        instance.markNeedsLayout();
    };

    @Override
    public Proxy<?> proxy() {
        return new Proxy<>(this, VISITOR);
    }
}
