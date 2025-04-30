package io.wispforest.owo.braid.widgets.sharedstate;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.function.Supplier;

public class SharedState<T extends SharableState> extends StatefulWidget {
    public final Supplier<T> initState;
    public final Widget child;

    public SharedState(Supplier<T> initState, Widget child) {
        this.initState = initState;
        this.child = child;
    }

    @Override
    public WidgetState<SharedState<T>> createState() {
        return new State<>();
    }

    public static class State<T extends SharableState> extends WidgetState<SharedState<T>> {
        public T state;
        public int generation = 0;

        @Override
        public void init() {
            super.init();
            state = widget().initState.get();
        }

        @Override
        public Widget build(BuildContext context) {
            return new SharedStateProvider<>(this, generation, widget().child);
        }
    }
}
