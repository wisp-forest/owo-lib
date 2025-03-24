package io.wispforest.owo.braid.framework.widget;

public abstract class InheritedWidget extends Widget {
    public final Widget child;

    protected InheritedWidget(Widget child) {
        this.child = child;
    }

    // ---

    public abstract boolean mustRebuildDependents(InheritedWidget oldWidget);
}
