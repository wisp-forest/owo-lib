package io.wispforest.owo.braid.core.events;

// TODO: stop requiring deltas in this event, the app state can easily compute them on its own
public record MouseMoveEvent(double x, double y, double deltaX, double deltaY) implements UserEvent {}
