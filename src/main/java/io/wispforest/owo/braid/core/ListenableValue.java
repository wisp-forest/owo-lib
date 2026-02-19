package io.wispforest.owo.braid.core;

public class ListenableValue<V> extends Listenable {

    private V value;

    public ListenableValue(V value) {
        this.value = value;
    }

    public V value() {
        return this.value;
    }

    public void setValue(V value) {
        this.value = value;
        this.notifyListeners();
    }
}
