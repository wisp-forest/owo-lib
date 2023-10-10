package io.wispforest.owo.serialization;

import java.util.Iterator;

public interface SequenceDeserializer<E> extends Iterator<E> {

    boolean hasNext();

    E next();
}
