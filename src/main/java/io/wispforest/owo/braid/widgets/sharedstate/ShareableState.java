package io.wispforest.owo.braid.widgets.sharedstate;

public abstract class ShareableState {
    SharedState.State<?> backingState;

    public final void setState(Runnable fn) {
        this.backingState.setState(() -> {
            fn.run();
            this.backingState.generation++;
        });
    }
}
