package io.wispforest.owo.braid.framework.instance;

import io.wispforest.owo.braid.framework.widget.InstanceWidget;

public abstract class LeafWidgetInstance<T extends InstanceWidget> extends WidgetInstance<T> {

    public LeafWidgetInstance(T widget) {
        super(widget);
    }

    @Override
    public void visitChildren(Visitor visitor) {}
}
