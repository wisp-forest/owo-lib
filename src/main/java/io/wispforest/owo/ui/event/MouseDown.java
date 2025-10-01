package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;
import net.minecraft.client.gui.Click;

public interface MouseDown {
    boolean onMouseDown(Click click, boolean doubled);

    static EventStream<MouseDown> newStream() {
        return new EventStream<>(subscribers -> (click, doubled) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseDown(click, doubled);
            }
            return anyTriggered;
        });
    }
}
