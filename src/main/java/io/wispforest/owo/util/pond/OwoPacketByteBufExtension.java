package io.wispforest.owo.util.pond;

import io.wispforest.owo.serialization.Endec;

public interface OwoPacketByteBufExtension {
    default <T> void write(Endec<T> endec, T value) {
        throw new UnsupportedOperationException();
    }

    default <T> T read(Endec<T> endec) {
        throw new UnsupportedOperationException();
    }
}
