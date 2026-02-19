package io.wispforest.owo.braid.widgets.sharedstate;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SharedState<T extends ShareableState> extends StatefulWidget {
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

    public static <T extends ShareableState> T get(BuildContext context, Class<T> clazz) {
        var provider = context.dependOnAncestor(SharedStateProvider.class, SharedStateProvider.keyOf(clazz));
        Preconditions.checkArgument(provider != null, "attempted to read shared state which is not provided by the current context");

        return (T) provider.state.state;
    }

    public static <T extends ShareableState> T getWithoutDependency(BuildContext context, Class<T> clazz) {
        var provider = context.getAncestor(SharedStateProvider.class, SharedStateProvider.keyOf(clazz));
        Preconditions.checkArgument(provider != null, "attempted to read shared state which is not provided by the current context");

        return (T) provider.state.state;
    }

    public static <T extends ShareableState, S> S select(BuildContext context, Class<T> clazz, Function<T, S> selector) {
        var provider = context.getAncestor(SharedStateProvider.class, SharedStateProvider.keyOf(clazz));
        Preconditions.checkArgument(provider != null, "attempted to select from shared state which is not provided by the current context");

        var capturedValue = selector.apply(((SharedStateProvider<T>) provider).state.state);
        context.dependOnAncestor(SharedStateProvider.class, SharedStateProvider.keyOf(clazz), SharedStateProvider.dependencyOf(clazz, capturedValue, selector));

        return capturedValue;
    }

    public static <T extends ShareableState> void set(BuildContext context, Class<T> clazz, Consumer<T> consumer) {
        var provider = context.dependOnAncestor(SharedStateProvider.class, SharedStateProvider.keyOf(clazz));
        Preconditions.checkArgument(provider != null, "attempted to set shared state which is not provided by the current context");

        provider.state.state.setState(() -> consumer.accept((T) provider.state.state));
    }

    public static class State<T extends ShareableState> extends WidgetState<SharedState<T>> {
        public T state;
        public int generation = 0;

        @Override
        public void init() {
            super.init();

            this.state = widget().initState.get();
            this.state.backingState = this;
        }

        @Override
        public Widget build(BuildContext context) {
            return new SharedStateProvider<>(this, this.generation, this.widget().child);
        }
    }
}
