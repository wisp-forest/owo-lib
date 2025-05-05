package io.wispforest.owo.braid.widgets.drag;

import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.Arrays;
import java.util.List;

public class DragArena extends MultiChildInstanceWidget {

    public DragArena(List<? extends Widget> children) {
        super(children);
    }

    public DragArena(Widget... children) {
        this(Arrays.asList(children));
    }

    @Override
    public MultiChildWidgetInstance<?> instantiate() {
        return new DragArenaInstance(this);
    }
}
