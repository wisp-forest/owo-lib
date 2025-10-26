package io.wispforest.owo.braid.framework.proxy;

import com.google.common.base.Preconditions;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public abstract class WidgetState<T extends StatefulWidget> {

    StatefulProxy owner;
    @Nullable T widget;

    public abstract Widget build(BuildContext context);

    public BuildContext context() {
        if (Owo.DEBUG) {
            Preconditions.checkNotNull(this.owner, "cannot access this.context() on a WidgetState before init() is called");
        }

        return this.owner;
    }

    public void init() {}
    public void dispose() {}

    public void didUpdateWidget(T oldWidget) {}
    public void notifyDependenciesChanged() {}

    public final void setState(Runnable fn) {
        Preconditions.checkState(this.owner != null, "setState invoked on WidgetState before it was mounted");

        fn.run();
        this.owner.markNeedsRebuild();
    }

    public final long scheduleDelayedCallback(Duration after, Runnable callback) {
        return this.owner.host().scheduleDelayedCallback(after, callback);
    }

    public final void cancelDelayedCallback(long id) {
        this.owner.host().cancelDelayedCallback(id);
    }

    public final void scheduleAnimationCallback(ProxyHost.AnimationCallback callback) {
        this.owner.host().scheduleAnimationCallback(callback);
    }

    public final void schedulePostLayoutCallback(Runnable callback) {
        this.owner.host().schedulePostLayoutCallback(callback);
    }

    public T widget() {
        Preconditions.checkNotNull(this.widget, "widget() accessor on a WidgetState was used before init()");
        return this.widget;
    }
}
