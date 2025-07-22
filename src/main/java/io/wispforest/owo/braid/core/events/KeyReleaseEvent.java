package io.wispforest.owo.braid.core.events;

import io.wispforest.owo.braid.core.KeyModifiers;

public record KeyReleaseEvent(int keycode, int scancode, KeyModifiers modifiers) implements UserEvent {}
