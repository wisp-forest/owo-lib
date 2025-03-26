package io.wispforest.owo.ui.event;

import io.wispforest.owo.util.EventStream;

public interface ComponentUpdate {

    void onUpdate(float delta, int mouseX, int mouseY);

    static EventStream<ComponentUpdate> newStream() {
        return new EventStream<>(subscribers -> (delta, mouseX, mouseY) -> {
            for (var subscriber : subscribers) {
                subscriber.onUpdate(delta, mouseX, mouseY);
            }
        });
    }
}
