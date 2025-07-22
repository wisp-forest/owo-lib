package io.wispforest.owo.braid.core.events;

public sealed interface UserEvent permits
    CloseEvent,
    CharInputEvent,
    FilesDroppedEvent,
    KeyPressEvent,
    KeyReleaseEvent,
    MouseButtonPressEvent,
    MouseButtonReleaseEvent,
    MouseMoveEvent,
    MouseScrollEvent {}
