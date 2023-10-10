package io.wispforest.owo.serialization;

public interface MapSerializer<V> extends Endable {

    void entry(String key, V value);

}
