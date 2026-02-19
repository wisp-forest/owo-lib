package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.proxy.InheritedProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;

public abstract class InheritedWidget extends Widget {
    public final Widget child;

    protected InheritedWidget(Widget child) {
        this.child = child;
    }

    @Override
    public WidgetProxy proxy() {
        return new InheritedProxy(this);
    }

    // ---

    public Object inheritedKey() {
        return this.getClass();
    }

    public abstract boolean mustRebuildDependents(InheritedWidget newWidget);
}
