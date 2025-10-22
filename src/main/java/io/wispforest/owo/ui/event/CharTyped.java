package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;
import net.minecraft.client.input.CharInput;

public interface CharTyped {
    boolean onCharTyped(CharInput input);

    static EventStream<CharTyped> newStream() {
        return new EventStream<>(subscribers -> (input) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onCharTyped(input);
            }
            return anyTriggered;
        });
    }
}
