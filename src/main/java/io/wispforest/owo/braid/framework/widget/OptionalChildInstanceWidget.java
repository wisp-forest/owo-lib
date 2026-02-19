package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.proxy.OptionalChildInstanceWidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import org.jetbrains.annotations.Nullable;

public abstract class OptionalChildInstanceWidget extends InstanceWidget {
    public final @Nullable Widget child;

    public OptionalChildInstanceWidget(@Nullable Widget child) {
        this.child = child;
    }

    @Override
    public abstract OptionalChildWidgetInstance<?> instantiate();

    @Override
    public WidgetProxy proxy() {
        return new OptionalChildInstanceWidgetProxy(this);
    }
}
