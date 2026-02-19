package io.wispforest.owo.braid.framework.proxy;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract non-sealed class InstanceWidgetProxy extends WidgetProxy {

    protected final WidgetInstance<InstanceWidget> instance;

    private final List<WidgetProxy> ancestorsUntilNextInstanceProxy = new ArrayList<>();

    protected InstanceWidgetProxy(InstanceWidget widget) {
        super(widget);

        //noinspection unchecked
        this.instance = (WidgetInstance<InstanceWidget>) widget.instantiate();
        Preconditions.checkNotNull(this.instance, "Widget#instantiate must return a non-null instance");
    }

    @Override
    public WidgetInstance<? extends InstanceWidget> instance() {
        return this.instance;
    }

    @Override
    public void mount(WidgetProxy parent, @Nullable Object slot) {
        super.mount(parent, slot);

        var ancestor = parent;
        while (!(ancestor instanceof InstanceWidgetProxy)) {
            this.ancestorsUntilNextInstanceProxy.add(ancestor);
            ancestor = ancestor.parent();
        }

        this.ancestorsUntilNextInstanceProxy.add(ancestor);

        this.rebuild();
        this.notifyAncestors();
    }

    @Override
    public void updateSlot(@Nullable Object newSlot) {
        super.updateSlot(newSlot);
        this.notifyAncestors();
    }

    @Override
    public void unmount() {
        super.unmount();
        this.instance.dispose();
        this.ancestorsUntilNextInstanceProxy.clear();
    }

    @Override
    public void updateWidget(Widget newWidget) {
        super.updateWidget(newWidget);
        this.instance.setWidget((InstanceWidget) newWidget);
    }

    private void notifyAncestors() {
        for (var listener : this.ancestorsUntilNextInstanceProxy) {
            listener.notifyDescendantInstance(this.instance, this.slot());
        }
    }
}
