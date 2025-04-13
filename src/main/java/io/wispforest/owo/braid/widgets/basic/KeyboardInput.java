package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class KeyboardInput extends SingleChildInstanceWidget {

    public KeyboardInput(Widget child) {
        super(child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return null;
    }
}
