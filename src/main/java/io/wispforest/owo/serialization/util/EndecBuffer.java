package io.wispforest.owo.serialization.util;

import io.wispforest.owo.serialization.Endec;

public interface EndecBuffer {
    default <T> void write(Endec<T> endec, T value) {
        throw new UnsupportedOperationException();
    }

    default <T> T read(Endec<T> endec) {
        throw new UnsupportedOperationException();
    }
}
