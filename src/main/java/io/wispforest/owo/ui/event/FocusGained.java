package io.wispforest.owo.ui.event;

import io.wispforest.owo.ui.core.Component;

public interface FocusGained {
    void onFocusGained(Component.FocusSource source);
}
