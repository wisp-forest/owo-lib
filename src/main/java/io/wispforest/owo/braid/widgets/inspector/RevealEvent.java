package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record RevealEvent(WidgetInstance<?> target, Set<WidgetInstance<?>> path) {
    public RevealEvent(WidgetInstance<?> target) {
        this(target, Stream.concat(target.ancestors().stream(), Stream.of(target)).collect(Collectors.toSet()));
    }
}
