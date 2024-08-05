package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;

public interface MouseScroll {
    boolean onMouseScroll(double mouseX, double mouseY, double amount);

    static EventStream<MouseScroll> newStream() {
        return new EventStream<>(subscribers -> (mouseX, mouseY, amount) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseScroll(mouseX, mouseY, amount);
            }
            return anyTriggered;
        });
    }
}
