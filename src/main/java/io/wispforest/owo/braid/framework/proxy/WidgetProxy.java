package io.wispforest.owo.braid.framework.proxy;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract sealed class WidgetProxy implements BuildContext, Comparable<WidgetProxy> permits ComposedProxy, InstanceWidgetProxy {

    private Widget widget;

    private @Nullable WidgetProxy parent;
    private BuildScope parentBuildScope;

    private int depth = -1;
    private @Nullable ProxyHost host;
    private @Nullable Object slot;
    public Lifecycle lifecycle = Lifecycle.INITIAL;
    protected boolean needsRebuild = true;

    private final Map<Class<?>, @Nullable InheritedProxy> dependencies = new HashMap<>();

    public WidgetProxy(Widget widget) {
        this.widget = widget;
        this.widget.freeze();
    }

    public void mount(WidgetProxy parent, @Nullable Object slot) {
        Preconditions.checkArgument(parent.mounted(), "parent proxy must be mounted before its children");

        Preconditions.checkState(this.lifecycle == Lifecycle.INITIAL, "proxy must be in INITIAL lifecycle state when mount() is called");
        this.lifecycle = Lifecycle.LIVE;

        this.parent = parent;
        this.parentBuildScope = parent.buildScope();
        this.setDepth(parent.depth + 1);
        this.slot = slot;
        this.host = parent.host;
    }

    @MustBeInvokedByOverriders
    public void updateSlot(@Nullable Object newSlot) {
        this.slot = newSlot;
    }

    public void unmount() {
        Preconditions.checkState(this.lifecycle == Lifecycle.LIVE, "proxy must be in LIVE lifecycle state when unmount() is called");
        this.lifecycle = Lifecycle.DEAD;

        for (var dependency : this.dependencies.values()) {
            if (dependency != null) dependency.removeDependent(this);
        }

        visitChildren(Visitors.UNMOUNT);
    }

    public void markNeedsRebuild() {
        if (this.needsRebuild) return;

        this.needsRebuild = true;
        this.buildScope().scheduleRebuild(this);
    }

    public void reassemble() {
        this.markNeedsRebuild();
        this.visitChildren(Visitors.REASSEMBLE);
    }

    // ---

    protected @Nullable WidgetProxy refreshChild(@Nullable WidgetProxy child, @Nullable Widget newWidget, @Nullable Object newSlot) {
        if (newWidget == null) {
            if (child != null) child.unmount();
            return null;
        }

        if (child != null && Widget.canUpdate(child.widget, newWidget)) {
            if (!Objects.equals(child.slot, newSlot)) {
                child.updateSlot(newSlot);
            }

            if (child.widget != newWidget) {
                child.updateWidget(newWidget);
            }

            return child;
        } else {
            if (child != null) {
                child.unmount();
            }

            var newProxy = newWidget.proxy();
            newProxy.mount(this, newSlot);
            return newProxy;
        }
    }

    @MustBeInvokedByOverriders
    public void updateWidget(Widget newWidget) {
        this.widget = newWidget;
        this.widget.freeze();
    }

    public final void rebuild() {
        this.rebuild(false);
    }

    public final void rebuild(boolean force) {
        if (!(force || (this.needsRebuild && this.lifecycle == Lifecycle.LIVE))) return;

        this.doRebuild();
    }

    @MustBeInvokedByOverriders
    protected void doRebuild() {
        this.needsRebuild = false;
    }

    // ---

    @Override
    public <T> T dependOnAncestor(Class<T> ancestorClass) {
        if (this.dependencies.containsKey(ancestorClass)) {
            var ancestor = this.dependencies.get(ancestorClass);
            if (ancestor == null) return null;

            //noinspection unchecked
            return (T) ancestor.widget();
        }

        var ancestor = this.parent;
        while (ancestor != null) {
            if (ancestor instanceof InheritedProxy inherited && inherited.widget().getClass() == ancestorClass) {
                inherited.addDependent(this);

                this.dependencies.put(ancestorClass, inherited);
                //noinspection unchecked
                return (T) inherited.widget();
            }

            ancestor = ancestor.parent;
        }

        this.dependencies.put(ancestorClass, null);
        return null;
    }

    public void notifyDependenciesChanged() {
        this.markNeedsRebuild();
    }

    // ---

    public abstract void visitChildren(Visitor visitor);

    // ---

    public Widget widget() {
        return this.widget;
    }

    public @Nullable WidgetProxy parent() {
        return this.parent;
    }

    public boolean mounted() {
        return this.parent != null;
    }

    public BuildScope buildScope() {
        Preconditions.checkNotNull(this.parentBuildScope, "parent build scope not set");
        return this.parentBuildScope;
    }

    public @Nullable Object slot() {
        return this.slot;
    }

    public @Nullable ProxyHost host() {
        return this.host;
    }

    public boolean needsRebuild() {
        return this.needsRebuild;
    }

    public int depth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        if (this.depth == depth) return;

        this.depth = depth;
        this.visitChildren(child -> child.setDepth(this.depth + 1));
    }

    // ---

    /// Set the host of this proxy, reserved for use by
    /// root proxy implementations. In all other scenarios,
    /// the host is to be taken from the parent in [#mount]
    protected void rootSetHost(ProxyHost host) {
        this.host = host;
    }

    // ---

    @Override
    public int compareTo(@NotNull WidgetProxy o) {
        return Integer.compare(this.depth, o.depth);
    }

    // ---

    @FunctionalInterface
    public interface Visitor {
        void visit(WidgetProxy child);
    }

    public enum Lifecycle {
        INITIAL, LIVE, DEAD
    }
}

enum Visitors implements WidgetProxy.Visitor {
    UNMOUNT(WidgetProxy::unmount),
    REASSEMBLE(WidgetProxy::reassemble);

    private final WidgetProxy.Visitor delegate;

    Visitors(WidgetProxy.Visitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visit(WidgetProxy child) {
        this.delegate.visit(child);
    }
}