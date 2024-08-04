package io.wispforest.owo.ui.event;

import Z;
import io.wispforest.owo.util.EventStream;

public interface MouseDrag {
    boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button);

    static EventStream<MouseDrag> newStream() {
        return new EventStream<>(subscribers -> (mouseX, mouseY, deltaX, deltaY, button) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
            }
            return anyTriggered;
        });
    }
}
