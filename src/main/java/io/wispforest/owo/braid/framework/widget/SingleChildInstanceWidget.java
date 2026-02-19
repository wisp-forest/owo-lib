package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.proxy.SingleChildInstanceWidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;

public abstract class SingleChildInstanceWidget extends InstanceWidget {

    public final Widget child;

    protected SingleChildInstanceWidget(Widget child) {
        this.child = child;
    }

    @Override
    public abstract SingleChildWidgetInstance<?> instantiate();

    @Override
    public WidgetProxy proxy() {
        return new SingleChildInstanceWidgetProxy(this);
    }
}
