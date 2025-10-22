package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;
import net.minecraft.client.gui.Click;

public interface MouseUp {
    boolean onMouseUp(Click click);

    static EventStream<MouseUp> newStream() {
        return new EventStream<>(subscribers -> (click) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseUp(click);
            }
            return anyTriggered;
        });
    }
}
