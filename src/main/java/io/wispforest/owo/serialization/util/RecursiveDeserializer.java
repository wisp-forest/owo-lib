package io.wispforest.owo.serialization.util;

import io.wispforest.owo.serialization.Deserializer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A template class for implementing deserializers which consume an
 * instance of some recursive data structure (like JSON, NBT or EDM)
 * <p>
 * Importantly, this class also supplies an implementation for {@link #tryRead(Function)}
 * which backs up the decoding frames and restores them upon failure. If this, for some reason,
 * is not the appropriate behavior for your input format, provide a custom implementation
 * <p>
 * Check {@link io.wispforest.owo.serialization.format.edm.EdmSerializer} or
 * {@link io.wispforest.owo.serialization.format.json.JsonDeserializer} for some reference
 * implementations
 */
public abstract class RecursiveDeserializer<T> implements Deserializer<T> {

    protected final Deque<Frame<T>> frames = new ArrayDeque<>();
    protected final T serialized;

    protected RecursiveDeserializer(T serialized) {
        this.serialized = serialized;
        this.frames.push(new Frame<>(() -> this.serialized, false));
    }

    /**
     * Get the value currently to be decoded
     * <p>
     * This value is altered by {@link #frame(Supplier, Supplier, boolean)} and
     * initially returns the entire serialized input
     */
    protected T getValue() {
        return this.frames.peek().source.get();
    }

    /**
     * Whether this deserializer is currently decoding a field
     * of a struct - useful for, for instance, an optimized optional representation
     * by skipping the field to indicate an absent optional
     */
    protected boolean isReadingStructField() {
        return this.frames.peek().isStructField;
    }

    /**
     * Decode the next value down the tree, given by {@code nextValue}, by pushing that frame
     * onto the decoding stack, invoking {@code action}, and popping the frame again. Consequently,
     * all decoding of {@code nextValue} must happen inside {@code action}
     * <p>
     * If {@code nextValue} is reading the field of a struct, {@code isStructField} must be set
     */
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
        var framesBackup = new ArrayDeque<>(this.frames);

        try {
            return reader.apply(this);
        } catch (Exception e) {
            this.frames.clear();
            this.frames.addAll(framesBackup);

            throw e;
        }
    }

    protected record Frame<T>(Supplier<T> source, boolean isStructField) {}
}
