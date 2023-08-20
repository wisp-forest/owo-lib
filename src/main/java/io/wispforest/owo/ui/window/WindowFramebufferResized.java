package io.wispforest.owo.ui.window;

import io.wispforest.owo.util.EventStream;

public interface WindowFramebufferResized {
    void onFramebufferResized(int newWidth, int newHeight);

    static EventStream<WindowFramebufferResized> newStream() {
        return new EventStream<>(subscribers -> (newWidth, newHeight) -> {
            for (var subscriber : subscribers) {
                subscriber.onFramebufferResized(newWidth, newHeight);
            }
        });
    }
}
