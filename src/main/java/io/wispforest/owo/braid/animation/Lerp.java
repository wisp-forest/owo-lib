package io.wispforest.owo.braid.animation;

public abstract class Lerp<T> {

    public final T start;
    public final T end;

    protected Lerp(T start, T end) {
        this.start = start;
        this.end = end;
    }

    public T compute(double t) {
        if (t - EPSILON <= 0) return this.start;
        if (t + EPSILON >= 1) return this.end;

        return this.at(t);
    }

    protected abstract T at(double t);

    // ---

    private static final double EPSILON = 1e-4;

    // ---

    @FunctionalInterface
    public interface Factory<T extends Lerp<V>, V> {
        T make(V start, V end);
    }
}
