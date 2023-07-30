package io.wispforest.owo.ui.window;

import io.wispforest.owo.util.EventStream;

public interface WindowMouseMoved {
    void onMouseMoved(double x, double y);

    static EventStream<WindowMouseMoved> newStream() {
        return new EventStream<>(subscribers -> (x, y) -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseMoved(x, y);
            }
        });
    }
}
