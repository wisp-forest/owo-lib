package io.wispforest.owo.serialization.util;

public interface Endable extends AutoCloseable {

    void end();

    @Override
    default void close() {
        this.end();
    }
}
