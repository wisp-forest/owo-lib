package io.wispforest.owo.braid.widgets.eventstream;

import io.wispforest.owo.util.EventStream;

public class BraidEventStream<T> extends EventStream<BraidEventStream.Listener<T>> {

    public BraidEventStream() {
        super(listeners -> event -> {
            for (var listener : listeners) listener.onEvent(event);
        });
    }

    @Override
    public BraidEventSource<T> source() {
        return new BraidEventSource<>(this);
    }

    public interface Listener<T> {
        void onEvent(T event);
    }
}
