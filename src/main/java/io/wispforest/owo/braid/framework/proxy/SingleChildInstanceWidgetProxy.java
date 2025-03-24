package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class SingleChildInstanceWidgetProxy extends InstanceWidgetProxy {
    protected WidgetProxy child;

    public SingleChildInstanceWidgetProxy(SingleChildInstanceWidget widget) {
        super(widget);
    }

    @Override
    public void mount(WidgetProxy parent, @Nullable Object slot) {
        super.mount(parent, slot);
        this.rebuild();
    }

    @Override
    public void updateWidget(Widget newWidget) {
        super.updateWidget(newWidget);
        this.rebuild(true);
    }

    @Override
    protected void doRebuild() {
        this.child = this.refreshChild(this.child, ((SingleChildInstanceWidget) this.widget()).child, null);
        super.doRebuild();
    }

    @Override
    public void notifyDescendantInstance(@Nullable WidgetInstance<?> instance, @Nullable Object slot) {
//        this.instance
    }

    @Override
    public void visitChildren(Visitor visitor) {
        if (this.child != null) visitor.visit(this.child);
    }
}
