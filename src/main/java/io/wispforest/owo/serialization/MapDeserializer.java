package io.wispforest.owo.serialization;

import java.util.Iterator;
import java.util.Map;

public interface MapDeserializer<E> extends Iterator<Map.Entry<String, E>> {

    boolean hasNext();

    Map.Entry<String, E> next();


}
