package io.wispforest.owo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EventStream<T> {

    protected final Function<List<T>, T> sinkFactory;
    protected final List<T> subscribers = new ArrayList<>();
    protected final EventSource<T> source = new EventSource<>(this);
    protected T sink;

    public EventStream(Function<List<T>, T> sinkFactory) {
        this.sinkFactory = sinkFactory;
        this.regenerateSink();
    }

    public T sink() {
        return this.sink;
    }

    public EventSource<T> source() {
        return this.source;
    }

    protected void addSubscriber(T subscriber) {
        this.subscribers.add(subscriber);
        this.regenerateSink();
    }

    protected void removeSubscriber(T subscriber) {
        this.subscribers.remove(subscriber);
        this.regenerateSink();
    }

    protected void regenerateSink() {
        this.sink = this.sinkFactory.apply(this.subscribers);
    }

}
