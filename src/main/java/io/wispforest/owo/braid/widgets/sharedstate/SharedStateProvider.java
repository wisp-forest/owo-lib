package io.wispforest.owo.braid.widgets.sharedstate;

import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class SharedStateProvider<T extends SharableState> extends InheritedWidget {
    public final SharedState.State<T> state;
    public final int generation;

    public SharedStateProvider(SharedState.State<T> state, int generation, Widget child) {
        super(child);
        this.state = state;
        this.generation = generation;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return generation != ((SharedStateProvider<?>)newWidget).generation;
    }
}
