package io.wispforest.owo.braid.animation;

import org.jetbrains.annotations.Nullable;

public class NullableLerp<T> extends Lerp<T> {

    private final @Nullable Lerp<T> delegate;

    public NullableLerp(@Nullable T start, @Nullable T end, Lerp.Factory<Lerp<T>, T> delegateFactory) {
        super(start, end);
        if (start != null) {
            this.delegate = delegateFactory.make(start, end);
        } else {
            this.delegate = null;
        }
    }

    @Override
    protected T at(double t) {
        return this.delegate != null ? this.delegate.at(t) : this.end;
    }
}
