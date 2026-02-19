package io.wispforest.owo.ui.event;

import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.util.EventStream;

public interface FocusGained {
    void onFocusGained(UIComponent.FocusSource source);

    static EventStream<FocusGained> newStream() {
        return new EventStream<>(subscribers -> source -> {
            for (var subscriber : subscribers) {
                subscriber.onFocusGained(source);
            }
        });
    }
}
