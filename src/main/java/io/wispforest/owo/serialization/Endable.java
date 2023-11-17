package io.wispforest.owo.serialization;

public interface Endable extends AutoCloseable {

    void end();

    @Override
    default void close() {
        this.end();
    }
}
