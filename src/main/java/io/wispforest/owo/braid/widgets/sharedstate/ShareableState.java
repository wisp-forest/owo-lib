package io.wispforest.owo.braid.widgets.sharedstate;

import io.wispforest.owo.braid.core.Listenable;

public abstract class ShareableState extends Listenable {

    public void setState(Runnable fn) {
        fn.run();
        this.notifyListeners();
    }

    public ShareableState self() {
        return this;
    }
}
