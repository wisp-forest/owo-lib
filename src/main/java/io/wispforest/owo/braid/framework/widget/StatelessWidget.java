package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.StatelessProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;

public abstract class StatelessWidget extends Widget {

    public abstract Widget build(BuildContext context);

    @Override
    public WidgetProxy proxy() {
        return new StatelessProxy(this);
    }
}
