package io.wispforest.owo.ui.event;

import Z;
import io.wispforest.owo.util.EventStream;

public interface CharTyped {
    boolean onCharTyped(char chr, int modifiers);

    static EventStream<CharTyped> newStream() {
        return new EventStream<>(subscribers -> (chr, modifiers) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onCharTyped(chr, modifiers);
            }
            return anyTriggered;
        });
    }
}
