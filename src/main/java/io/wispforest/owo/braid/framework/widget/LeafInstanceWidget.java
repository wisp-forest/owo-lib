package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.proxy.LeafInstanceWidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;

public abstract class LeafInstanceWidget extends InstanceWidget {

    @Override
    public abstract LeafWidgetInstance<?> instantiate();

    @Override
    public WidgetProxy proxy() {
        return new LeafInstanceWidgetProxy(this);
    }
}
