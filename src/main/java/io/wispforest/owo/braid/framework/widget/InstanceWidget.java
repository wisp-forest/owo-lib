package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;

public abstract class InstanceWidget extends Widget {
    public abstract WidgetInstance<?> instantiate();
}
