package io.wispforest.owo.braid.widgets.focus;

import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

class FocusStateProvider<F extends Focusable.State<?>> extends InheritedWidget {

    public final F state;
    public final @Nullable FocusLevel level;

    private final InheritedKey inheritedKey;

    public FocusStateProvider(F state, Class<F> stateClass, @Nullable FocusLevel level, Widget child) {
        super(child);
        this.state = state;
        this.level = level;

        this.inheritedKey = new InheritedKey(stateClass);
    }

    @Override
    public Object inheritedKey() {
        return this.inheritedKey;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        //noinspection unchecked
        return ((FocusStateProvider<F>) newWidget).level != this.level;
    }

    // ---

    public static <F extends Focusable.State<?>> Object keyOf(Class<F> stateClass) {
        return new InheritedKey(stateClass);
    }
}

record InheritedKey(Class<?> stateClass) {}
