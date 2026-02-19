package io.wispforest.owo.braid.widgets.eventstream;

import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class StreamListenerState<T extends StatefulWidget> extends WidgetState<T> {
    private final List<SubscriptionData<T, ?>> streamSubscriptions = new ArrayList<>();

    protected <S> void streamListen(Function<T, @Nullable BraidEventSource<S>> streamGetter, Consumer<S> onData) {
        this.streamSubscriptions.add(
            new SubscriptionData<>(this.widget(), streamGetter, stream -> stream.subscribe(onData::accept))
        );
    }

    @Override
    public void didUpdateWidget(T oldWidget) {
        super.didUpdateWidget(oldWidget);

        for (var subscription : this.streamSubscriptions) {
            subscription.update(this.widget());
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        for (var subscription : this.streamSubscriptions) {
            if (subscription.currentSubscription != null) subscription.currentSubscription.cancel();
        }
    }


    private static class SubscriptionData<W, T> {
        private final Function<W, @Nullable BraidEventSource<T>> getter;
        private final Function<BraidEventSource<T>, BraidEventSource<T>.Subscription> listenerFactory;

        private @Nullable BraidEventSource<T> currentStream;
        private @Nullable BraidEventSource<T>.Subscription currentSubscription;

        private SubscriptionData(W widget, Function<W, @Nullable BraidEventSource<T>> getter, Function<BraidEventSource<T>, BraidEventSource<T>.Subscription> listenerFactory) {
            this.getter = getter;
            this.listenerFactory = listenerFactory;

            this.listenOn(widget);
        }

        private void listenOn(W widget) {
            this.currentStream = this.getter.apply(widget);
            if (this.currentStream == null) return;

            this.currentSubscription = this.listenerFactory.apply(this.currentStream);
        }

        public void update(W newWidget) {
            var newStream = this.getter.apply(newWidget);
            if (newStream == this.currentStream) return;

            if (this.currentSubscription != null) this.currentSubscription.cancel();
            listenOn(newWidget);
        }
    }
}
