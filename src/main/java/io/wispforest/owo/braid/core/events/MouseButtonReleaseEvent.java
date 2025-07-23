package io.wispforest.owo.braid.core.events;

import io.wispforest.owo.braid.core.KeyModifiers;

public record MouseButtonReleaseEvent(int button, KeyModifiers modifiers) implements UserEvent {
    public MouseButtonReleaseEvent(int button, int modifiers) {
        this(button, new KeyModifiers(modifiers));
    }
}
