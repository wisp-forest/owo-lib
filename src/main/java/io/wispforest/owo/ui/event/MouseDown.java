package io.wispforest.owo.ui.event;

import Z;
import io.wispforest.owo.util.EventStream;

public interface MouseDown {
    boolean onMouseDown(double mouseX, double mouseY, int button);

    static EventStream<MouseDown> newStream() {
        return new EventStream<>(subscribers -> (mouseX, mouseY, button) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseDown(mouseX, mouseY, button);
            }
            return anyTriggered;
        });
    }
}
