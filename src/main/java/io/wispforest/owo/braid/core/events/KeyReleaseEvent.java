package io.wispforest.owo.braid.core.events;

import io.wispforest.owo.braid.core.KeyModifiers;

public record KeyReleaseEvent(int keycode, int scancode, KeyModifiers modifiers) implements UserEvent {
    public KeyReleaseEvent(int keycode, int scancode, int modifiers) {
        this(keycode, scancode, new KeyModifiers(modifiers));
    }
}
