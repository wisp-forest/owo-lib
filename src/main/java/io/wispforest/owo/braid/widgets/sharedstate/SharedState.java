package io.wispforest.owo.braid.widgets.sharedstate;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.function.Consumer;
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

    //TODO glisco fix this at some point
    public static <T extends ShareableState> T get(BuildContext context, Class<T> clazz) {
        var provider = context.dependOnAncestor(SharedStateProvider.class);
        Preconditions.checkArgument(provider != null, "attempted to read inherited state which is not provided by the current context");

        //TODO its right there VVVV
        return (T) provider.state.state;
    }

    //TODO also fix this one
    public static <T extends ShareableState> void set(BuildContext context, Class<T> clazz, Consumer<T> consumer) {
        var provider = context.dependOnAncestor(SharedStateProvider.class);
        Preconditions.checkArgument(provider != null, "attempted to set inherited state which is not provided by the current context");

        //TODO this one is there tho --------VVVV
        provider.state.setState(() -> {
            consumer.accept((T) provider.state.state);
            provider.state.generation++;
        });

    }

    public static class State<T extends ShareableState> extends WidgetState<SharedState<T>> {
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
