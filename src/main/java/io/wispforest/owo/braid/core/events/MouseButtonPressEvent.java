package io.wispforest.owo.braid.core.events;

import io.wispforest.owo.braid.core.KeyModifiers;

public record MouseButtonPressEvent(int button, KeyModifiers modifiers) implements UserEvent {
    public MouseButtonPressEvent(int button, int modifiers) {
        this(button, new KeyModifiers(modifiers));
    }
}
