package io.wispforest.owo.braid.widgets.eventstream;

import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;

public class BraidEventSource<T> extends EventSource<BraidEventStream.Listener<T>> {
    BraidEventSource(EventStream<BraidEventStream.Listener<T>> stream) {
        super(stream);
    }
}
