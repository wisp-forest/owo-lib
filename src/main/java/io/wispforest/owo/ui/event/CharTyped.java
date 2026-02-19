package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;
import net.minecraft.client.input.CharacterEvent;

public interface CharTyped {
    boolean onCharTyped(CharacterEvent input);

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
