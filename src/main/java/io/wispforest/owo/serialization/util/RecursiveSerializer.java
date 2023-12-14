package io.wispforest.owo.serialization.util;

import io.wispforest.owo.serialization.Serializer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * A template class for implementing serializers which produce as result an
 * instance of some recursive data structure (like JSON, NBT or EDM)
 * <p>
 * Check {@link io.wispforest.owo.serialization.format.edm.EdmSerializer} or
 * {@link io.wispforest.owo.serialization.format.json.JsonSerializer} for some reference
 * implementations
 */
public abstract class RecursiveSerializer<T> implements Serializer<T> {

    protected final Deque<Frame<T>> frames = new ArrayDeque<>();
    protected T result;

    protected RecursiveSerializer(T initialResult) {
        this.result = initialResult;
        this.frames.push(new Frame<>(t -> this.result = t, false));
    }

    /**
     * Store {@code value} into the current encoding location
     * <p>
     * This location is altered by {@link #frame(FrameAction, boolean)} and
     * initially is just the serializer's result directly
     */
    protected void consume(T value) {
        this.frames.peek().sink.accept(value);
    }

    /**
     * Whether this deserializer is currently decoding a field
     * of a struct - useful for, for instance, an optimized optional representation
     * by skipping the field to indicate an absent optional
     */
    protected boolean isWritingStructField() {
        return this.frames.peek().isStructField;
    }

    /**
     * Encode the next value down the tree by pushing a new frame
     * onto the encoding stack and invoking {@code action}
     * <p>
     * {@code action} receives {@code encoded}, which is where the next call
     * to {@link #consume(Object)} (which {@code action} must somehow cause) will
     * store the value and allow {@code action} to retrieve it using {@link EncodedValue#get()}
     * or, preferably, {@link EncodedValue#require(String)}
     */
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
