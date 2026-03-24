package io.wispforest.owo.braid.widgets.inspector;

import java.util.Set;

public record RevealEvent<T>(T target, Set<T> path) {}
