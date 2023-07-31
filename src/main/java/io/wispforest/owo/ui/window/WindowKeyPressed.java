package io.wispforest.owo.ui.window;

import io.wispforest.owo.util.EventStream;

public interface WindowKeyPressed {
    void onKeyPressed(int keyCode, int scanCode, int modifiers, boolean released);

    static EventStream<WindowKeyPressed> newStream() {
        return new EventStream<>(subscribers -> (keyCode, scanCode, modifiers, released) -> {
            for (var subscriber : subscribers) {
                subscriber.onKeyPressed(keyCode, scanCode, modifiers, released);
            }
        });
    }
}
