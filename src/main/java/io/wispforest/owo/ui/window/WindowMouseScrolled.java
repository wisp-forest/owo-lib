package io.wispforest.owo.ui.window;

import io.wispforest.owo.util.EventStream;

public interface WindowMouseScrolled {
    void onMouseScrolled(double xOffset, double yOffset);

    static EventStream<WindowMouseScrolled> newStream() {
        return new EventStream<>(subscribers -> (xOffset, yOffset) -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseScrolled(xOffset, yOffset);
            }
        });
    }
}
