package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RevealInstanceEvent {
    public final WidgetInstance<?> instance;
    public final Set<WidgetInstance<?>> fullPath;

    public RevealInstanceEvent(WidgetInstance<?> instance) {
        this.instance = instance;
        this.fullPath = Stream.concat(
            this.instance.ancestors().stream(),
            Stream.of(this.instance)
        ).collect(Collectors.toSet());
    }
}
