package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;

public interface MouseLeave {
    void onMouseLeave();

    static EventStream<MouseLeave> newStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseLeave();
            }
        });
    }
}
