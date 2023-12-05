package io.wispforest.owo.serialization.util;

import io.wispforest.owo.serialization.Serializer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public abstract class HierarchicalSerializer<T> implements Serializer<T> {

    protected final Deque<Frame<T>> frames = new ArrayDeque<>();
    protected T result;

    protected HierarchicalSerializer(T initialResult) {
        this.result = initialResult;
        this.frames.push(new Frame<>(t -> this.result = t, false));
    }

    protected void consume(T value) {
        this.frames.peek().sink.accept(value);
    }

    protected boolean isWritingStructField() {
        return this.frames.peek().isStructField;
    }

    protected void frame(FrameAction<T> action, boolean isStructField) {
        var encoded = new EncodedValue<T>();

        this.frames.push(new Frame<>(encoded::set, isStructField));
        action.accept(encoded);
        this.frames.pop();
    }

    @Override
    public T result() {
        return this.result;
    }

    @FunctionalInterface
    protected interface FrameAction<T> {
        void accept(EncodedValue<T> encoded);
    }

    protected record Frame<T>(Consumer<T> sink, boolean isStructField) {}

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

        public boolean wasEncoded() {
            return this.encoded;
        }

        public T require(String name) {
            if (!this.encoded) throw new IllegalStateException("Endec for " + name + " serialized nothing");
            return this.value;
        }
    }
}
