package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.proxy.StatefulProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetState;

public abstract class StatefulWidget extends Widget {
    public abstract WidgetState<?> createState();

    @Override
    public WidgetProxy proxy() {
        return new StatefulProxy(this);
    }
}
