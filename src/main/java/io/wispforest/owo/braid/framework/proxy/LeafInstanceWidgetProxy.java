package io.wispforest.owo.braid.framework.proxy;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import org.jetbrains.annotations.Nullable;

public class LeafInstanceWidgetProxy extends InstanceWidgetProxy {
    public LeafInstanceWidgetProxy(LeafInstanceWidget widget) {
        super(widget);
    }

    @Override
    public void visitChildren(Visitor visitor) {}

    @Override
    public void notifyDescendantInstance(@Nullable WidgetInstance<?> instance, @Nullable Object slot) {
        Preconditions.checkState(false, "a leaf proxy cannot have descendant instances");
    }
}
