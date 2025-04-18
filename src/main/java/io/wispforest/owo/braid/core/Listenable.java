package io.wispforest.owo.braid.core;

import java.util.ArrayList;
import java.util.List;

public abstract class Listenable {

    protected final List<Runnable> listeners = new ArrayList<>();

    public void addListener(Runnable listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        this.listeners.remove(listener);
    }

    protected void notifyListeners() {
        this.listeners.forEach(Runnable::run);
    }
}
