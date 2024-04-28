package io.wispforest.owo.serialization.util;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationContext;

public interface EndecBuffer {
    default <T> void write(SerializationContext ctx, Endec<T> endec, T value) {
        throw new UnsupportedOperationException();
    }

    default <T> void write(Endec<T> endec, T value) {
        this.write(SerializationContext.empty(), endec, value);
    }

    default <T> T read(SerializationContext ctx, Endec<T> endec) {
        throw new UnsupportedOperationException();
    }

    default <T> T read(Endec<T> endec) {
        return this.read(SerializationContext.empty(), endec);
    }
}
