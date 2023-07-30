package io.wispforest.owo.ui.window;

import io.wispforest.owo.util.EventStream;

public interface WindowResized {
    void onWindowResized(int newWidth, int newHeight);

    static EventStream<WindowResized> newStream() {
        return new EventStream<>(subscribers -> (newWidth, newHeight) -> {
            for (var subscriber : subscribers) {
                subscriber.onWindowResized(newWidth, newHeight);
            }
        });
    }
}
