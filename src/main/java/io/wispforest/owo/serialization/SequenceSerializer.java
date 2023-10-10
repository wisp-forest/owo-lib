package io.wispforest.owo.serialization;

public interface SequenceSerializer<E> extends Endable {
    void element(E element);
}
