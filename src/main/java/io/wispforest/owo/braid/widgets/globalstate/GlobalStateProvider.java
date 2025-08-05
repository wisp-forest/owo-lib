package io.wispforest.owo.braid.widgets.globalstate;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class GlobalStateProvider<T extends GlobalState<T>> extends StatefulWidget {
    public final T state;
    public final Widget child;

    public GlobalStateProvider(T state, Widget child) {
        this.state = state;
        this.child = child;
    }

    @Override
    public WidgetState<GlobalStateProvider<T>> createState() {
        return new State<>();
    }

    public static class State<T extends GlobalState<T>> extends WidgetState<GlobalStateProvider<T>> {
        @Override
        public Widget build(BuildContext context) {
            return this.widget().child;
        }
    }
}
