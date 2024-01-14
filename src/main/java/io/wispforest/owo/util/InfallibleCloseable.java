package io.wispforest.owo.util;

public interface InfallibleCloseable extends AutoCloseable {
    static InfallibleCloseable empty() {
        return () -> {};
    }

    @Override
    void close();
}
