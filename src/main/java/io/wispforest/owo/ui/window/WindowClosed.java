package io.wispforest.owo.ui.window;

import io.wispforest.owo.util.EventStream;

public interface WindowClosed {
    void onWindowClosed();

    static EventStream<WindowClosed> newStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onWindowClosed();
            }
        });
    }
}
