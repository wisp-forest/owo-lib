package io.wispforest.owo.braid.framework.proxy;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract non-sealed class InstanceWidgetProxy extends WidgetProxy implements InstanceListenerProxy {

    protected final WidgetInstance<InstanceWidget> instance;

    private final List<InstanceListenerProxy> ancestorInstanceListeners = new ArrayList<>();

    protected InstanceWidgetProxy(InstanceWidget widget) {
        super(widget);

        //noinspection unchecked
        this.instance = (WidgetInstance<InstanceWidget>) widget.instantiate();
        Preconditions.checkNotNull(this.instance, "Widget#instantiate must return a non-null instance");
    }

    public WidgetInstance<? extends InstanceWidget> instance() {
        return this.instance;
    }

    @Override
    public void mount(WidgetProxy parent, @Nullable Object slot) {
        super.mount(parent, slot);

        var ancestor = parent;
        while (!(ancestor instanceof InstanceWidgetProxy)) {
            if (ancestor instanceof InstanceListenerProxy listener) this.ancestorInstanceListeners.add(listener);
            ancestor = ancestor.parent();
        }

        this.ancestorInstanceListeners.add((InstanceWidgetProxy) ancestor);

        rebuild();
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
        this.ancestorInstanceListeners.clear();
    }

    @Override
    public void updateWidget(Widget newWidget) {
        super.updateWidget(newWidget);
        this.instance.setWidget((InstanceWidget) newWidget);
    }

    @Override
    protected void doRebuild() {
        super.doRebuild();
        this.notifyAncestors();
    }

    private void notifyAncestors() {
        for (var listener : this.ancestorInstanceListeners) {
            listener.notifyDescendantInstance(this.instance, this.slot());
        }
    }
}
