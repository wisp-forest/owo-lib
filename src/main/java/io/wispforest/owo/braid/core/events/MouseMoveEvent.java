package io.wispforest.owo.braid.core.events;

public record MouseMoveEvent(double x, double y, double deltaX, double deltaY) implements UserEvent {}
