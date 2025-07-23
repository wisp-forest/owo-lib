package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.util.EventStream;

public class BraidEventStream<T> extends EventStream<BraidEventStream.Listener<T>> {

    public BraidEventStream() {
        super(listeners -> event -> {
            for (var listener : listeners) listener.onEvent(event);
        });
    }

    public interface Listener<T> {
        void onEvent(T event);
    }
}
