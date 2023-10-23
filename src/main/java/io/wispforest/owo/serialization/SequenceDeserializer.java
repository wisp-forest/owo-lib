package io.wispforest.owo.serialization;

import java.util.Iterator;

public interface SequenceDeserializer<E> extends Iterator<E> {

    int size();

    boolean hasNext();

    E next();
}
