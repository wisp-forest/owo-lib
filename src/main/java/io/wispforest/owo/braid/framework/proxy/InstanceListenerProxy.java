package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import org.jetbrains.annotations.Nullable;

public interface InstanceListenerProxy {
    void notifyDescendantInstance(@Nullable WidgetInstance<?> instance, @Nullable Object slot);
}
