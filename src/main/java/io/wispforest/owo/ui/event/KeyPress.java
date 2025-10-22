package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;
import net.minecraft.client.input.KeyInput;

public interface KeyPress {
    boolean onKeyPress(KeyInput input);

    static EventStream<KeyPress> newStream() {
        return new EventStream<>(subscribers -> (input) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onKeyPress(input);
            }
            return anyTriggered;
        });
    }
}
