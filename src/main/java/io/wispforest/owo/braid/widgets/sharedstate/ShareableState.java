package io.wispforest.owo.braid.widgets.sharedstate;

import io.wispforest.endec.StructEndec;
import io.wispforest.owo.braid.core.Listenable;

import java.util.function.Supplier;

public abstract class ShareableState extends Listenable implements Supplier<ShareableState> {

    public void setState(Runnable fn) {
        fn.run();
        this.notifyListeners();
    }

    // :3
    @Override
    public ShareableState get() {
        return this;
    }
}
