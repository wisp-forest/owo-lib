package io.wispforest.owo.braid.widgets.flex;

import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;

public class Flexible extends VisitorWidget {

    public final double flexFactor;

    public Flexible(double flexFactor, Widget child) {
        super(child);
        this.flexFactor = flexFactor;
    }

    public Flexible(Widget child) {
        this(1, child);
    }

    private static final Visitor<Flexible> VISITOR = (widget, instance) -> {
        if (instance.parentData instanceof FlexParentData data) {
            data.flexFactor = widget.flexFactor;
        } else {
            instance.parentData = new FlexParentData(widget.flexFactor);
        }

        instance.markNeedsLayout();
    };

    @Override
    public Proxy<?> proxy() {
        return new VisitorWidget.Proxy<>(this, VISITOR);
    }
}
