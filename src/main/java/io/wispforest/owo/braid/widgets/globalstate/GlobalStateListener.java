package io.wispforest.owo.braid.widgets.globalstate;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.function.Function;

public class GlobalStateListener<T extends GlobalState> extends StatefulWidget {

    private final T state;
    private final Function<T, Widget> builder;

    private GlobalStateListener(T state, Function<T, Widget> builder) {
        this.state = state;
        this.builder = builder;
    }

    public static <T extends GlobalState> GlobalStateListener<T> of(T state, Function<T, Widget> builder) {
        return new GlobalStateListener<>(state, builder);
    }

    @Override
    public WidgetState<GlobalStateListener<T>> createState() {
        return new State<>();
    }

    public static class State<T extends GlobalState> extends WidgetState<GlobalStateListener<T>> {

        private final Runnable listener = () -> setState(() -> {});

        @Override
        public void init() {
            widget().state.addListener(this.listener);
        }

        @Override
        public void dispose() {
            widget().state.removeListener(this.listener);
        }

        @Override
        public Widget build(BuildContext context) {
            return widget().builder.apply(widget().state);
        }
    }
}
