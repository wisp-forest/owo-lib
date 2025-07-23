package io.wispforest.owo.braid.core.events;

import io.wispforest.owo.braid.core.KeyModifiers;

public record KeyPressEvent(int keyCode, int scancode, KeyModifiers modifiers) implements UserEvent {
    public KeyPressEvent(int keyCode, int scancode, int modifiers) {
        this(keyCode, scancode, new KeyModifiers(modifiers));
    }
}
