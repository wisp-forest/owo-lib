package io.wispforest.owo.serialization;

/**
 * Interface that indicates that the object has an end operation to such
 */
public interface Endable extends AutoCloseable {

    void end();

    @Override default void close(){ end(); }
}
