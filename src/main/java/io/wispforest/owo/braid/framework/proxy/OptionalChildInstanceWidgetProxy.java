package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class OptionalChildInstanceWidgetProxy extends InstanceWidgetProxy {

    protected @Nullable WidgetProxy child;

    public OptionalChildInstanceWidgetProxy(OptionalChildInstanceWidget widget) {
        super(widget);
    }

    @Override
    public OptionalChildWidgetInstance<? extends InstanceWidget> instance() {
        return (OptionalChildWidgetInstance<? extends InstanceWidget>) super.instance();
    }

    @Override
    public void updateWidget(Widget newWidget) {
        super.updateWidget(newWidget);
        this.rebuild(true);
    }

    @Override
    protected void doRebuild() {
        super.doRebuild();
        this.child = this.refreshChild(this.child, ((OptionalChildInstanceWidget) this.widget()).child, null);

        if (((OptionalChildInstanceWidget) this.widget()).child == null) {
            this.instance().setChild(null);
        }
    }

    @Override
    public void notifyDescendantInstance(@Nullable WidgetInstance<?> instance, @Nullable Object slot) {
        this.instance().setChild(instance);
    }

    @Override
    public void visitChildren(Visitor visitor) {
        if (this.child != null) {
            visitor.visit(this.child);
        }
    }
}
