package io.wispforest.owo.braid.core.events;

import io.wispforest.owo.braid.core.KeyModifiers;

public record CharInputEvent(char codepoint, KeyModifiers modifiers) implements UserEvent {
    public CharInputEvent(char codepoint, int modifiers) {
        this(codepoint, new KeyModifiers(modifiers));
    }
}
