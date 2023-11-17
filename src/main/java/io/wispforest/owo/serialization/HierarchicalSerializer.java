package io.wispforest.owo.serialization;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public abstract class HierarchicalSerializer<T> implements Serializer<T> {

    protected final Deque<Consumer<T>> sinks = new ArrayDeque<>();
    protected T result;

    protected HierarchicalSerializer(T initialResult) {
        this.result = initialResult;
        this.sinks.push(t -> this.result = t);
    }

    protected void consume(T value) {
        this.sinks.peek().accept(value);
    }

    protected void frame(FrameAction<T> action) {
        var encoded = new EncodedValue<T>();

        this.sinks.push(encoded::set);
        action.accept(encoded);
        this.sinks.pop();
    }

    @FunctionalInterface
    protected interface FrameAction<T> {
        void accept(EncodedValue<T> encoded);
    }

    protected static class EncodedValue<T> {
        private T value = null;
        private boolean encoded = false;

        private void set(T value) {
            this.value = value;
            this.encoded = true;
        }

        public T get() {
            return this.value;
        }

        public T require(String name) {
            if (!this.encoded) throw new IllegalStateException("Endec for " + name + " serialized nothing");
            return this.value;
        }
    }
}
