package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.proxy.MultiChildInstanceWidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;

import java.util.List;

public abstract class MultiChildInstanceWidget extends InstanceWidget {
    public final List<? extends Widget> children;

    protected MultiChildInstanceWidget(List<? extends Widget> children) {
        this.children = children;
    }

    @Override
    public abstract MultiChildWidgetInstance<?> instantiate();

    @Override
    public WidgetProxy proxy() {
        return new MultiChildInstanceWidgetProxy(this);
    }
}
