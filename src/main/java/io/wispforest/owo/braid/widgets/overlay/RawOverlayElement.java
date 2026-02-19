package io.wispforest.owo.braid.widgets.overlay;

import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;

public class RawOverlayElement extends VisitorWidget {
    public final double x, y;

    public RawOverlayElement(double x, double y, Widget child) {
        super(child);
        this.x = x;
        this.y = y;
    }

    public static final Visitor<RawOverlayElement> VISITOR = (widget, instance) -> {
        if (instance.parentData instanceof OverlayParentData data) {
            data.x = widget.x;
            data.y = widget.y;
        } else {
            instance.parentData = new OverlayParentData(widget.x, widget.y);
        }

        instance.markNeedsLayout();
    };

    @Override
    public Proxy<?> proxy() {
        return new Proxy<>(this, VISITOR);
    }
}
