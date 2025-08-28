package io.wispforest.owo.braid.core;

public class ListenableValue<V> extends Listenable {

    private V value;

    public ListenableValue(V value) {
        this.value = value;
    }

    public V get() {
        return this.value;
    }

    public void set(V value) {
        this.value = value;
        this.notifyListeners();
    }
}
