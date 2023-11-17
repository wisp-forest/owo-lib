package io.wispforest.owo.serialization;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class HierarchicalDeserializer<T> implements Deserializer<T> {

    protected final Deque<Supplier<T>> sources = new ArrayDeque<>();
    protected final T serialized;

    protected HierarchicalDeserializer(T serialized) {
        this.serialized = serialized;
        this.sources.push(() -> this.serialized);
    }

    protected T getValue() {
        return this.sources.peek().get();
    }

    protected <V> V frame(Supplier<T> nextValue, Supplier<V> action) {
        try {
            this.sources.push(nextValue);
            return action.get();
        } finally {
            this.sources.pop();
        }
    }

    @Override
    public <V> V tryRead(Function<Deserializer<T>, V> reader) {
        var sourcesBackup = new ArrayDeque<>(this.sources);

        try {
            return reader.apply(this);
        } catch (Exception e) {
            this.sources.clear();
            this.sources.addAll(sourcesBackup);

            throw e;
        }
    }
}
