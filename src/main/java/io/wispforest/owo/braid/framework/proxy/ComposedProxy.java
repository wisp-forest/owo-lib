package io.wispforest.owo.braid.framework.proxy;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public abstract non-sealed class ComposedProxy extends WidgetProxy {

    protected @Nullable WidgetProxy child;

    public ComposedProxy(Widget widget) {
        super(widget);
    }

    public WidgetProxy child() {
        return this.child;
    }

    @Override
    public void visitChildren(Visitor visitor) {
        if (this.child != null) visitor.visit(this.child);
    }

    // ---

    private WidgetInstance<?> descendantInstance;

    @Override
    public @Nullable WidgetInstance<?> instance() {
        return this.descendantInstance;
    }

    @Override
    public void notifyDescendantInstance(@Nullable WidgetInstance<?> instance, @Nullable Object slot) {
        this.descendantInstance = instance;
    }
}
