package io.wispforest.owo.braid.core.events;

import io.wispforest.owo.braid.core.KeyModifiers;

public record KeyPressEvent(int keyCode, int scancode, KeyModifiers modifiers) implements UserEvent {}
