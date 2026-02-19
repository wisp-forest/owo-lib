package io.wispforest.owo.braid.core;

import java.util.ArrayList;
import java.util.List;

public class CompoundListenable extends Listenable {

    protected final Runnable listener = this::notifyListeners;
    protected final List<Listenable> children = new ArrayList<>();

    public CompoundListenable(Listenable... initialChildren) {
        for (var child : initialChildren) {
            this.addChild(child);
        }
    }

    public void addChild(Listenable child) {
        this.children.add(child);
        child.addListener(this.listener);
    }

    public void removeChild(Listenable child) {
        this.children.remove(child);
        child.removeListener(this.listener);
    }

    public void clear() {
        for (var child : this.children) {
            child.removeListener(this.listener);
        }

        this.children.clear();
    }
}
