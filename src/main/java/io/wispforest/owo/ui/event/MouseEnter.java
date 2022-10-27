package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;

public interface MouseEnter {
    void onMouseEnter();

    static EventStream<MouseEnter> newStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseEnter();
            }
        });
    }
}
