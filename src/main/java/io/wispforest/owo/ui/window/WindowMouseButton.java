package io.wispforest.owo.ui.window;

import io.wispforest.owo.util.EventStream;

public interface WindowMouseButton {
    void onMouseButton(int button, boolean released);

    static EventStream<WindowMouseButton> newStream() {
        return new EventStream<>(subscribers -> (button, released) -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseButton(button, released);
            }
        });
    }
}
