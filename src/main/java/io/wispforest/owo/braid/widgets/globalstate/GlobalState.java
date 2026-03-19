package io.wispforest.owo.braid.widgets.globalstate;

import io.wispforest.owo.braid.core.Listenable;

public abstract class GlobalState extends Listenable {

    public final void setState(Runnable fn) {
        fn.run();
        this.notifyListeners();
    }
}
