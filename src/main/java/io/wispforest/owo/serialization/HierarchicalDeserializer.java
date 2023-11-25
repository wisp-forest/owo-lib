package io.wispforest.owo.serialization;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class HierarchicalDeserializer<T> implements Deserializer<T> {

    protected final Deque<Frame<T>> frames = new ArrayDeque<>();
    protected final T serialized;

    protected HierarchicalDeserializer(T serialized) {
        this.serialized = serialized;
        this.frames.push(new Frame<>(() -> this.serialized, false));
    }

    protected T getValue() {
        return this.frames.peek().source.get();
    }

    protected boolean isReadingStructField() {
        return this.frames.peek().isStructField;
    }

    protected <V> V frame(Supplier<T> nextValue, Supplier<V> action, boolean isStructField) {
        try {
            this.frames.push(new Frame<>(nextValue, isStructField));
            return action.get();
        } finally {
            this.frames.pop();
        }
    }

    @Override
    public <V> V tryRead(Function<Deserializer<T>, V> reader) {
        var sourcesBackup = new ArrayDeque<>(this.frames);

        try {
            return reader.apply(this);
        } catch (Exception e) {
            this.frames.clear();
            this.frames.addAll(sourcesBackup);

            throw e;
        }
    }

    protected record Frame<T>(Supplier<T> source, boolean isStructField) {}
}
