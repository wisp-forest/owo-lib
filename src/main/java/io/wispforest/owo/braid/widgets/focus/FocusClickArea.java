package io.wispforest.owo.braid.widgets.focus;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class FocusClickArea extends SingleChildInstanceWidget {

    public final Runnable clickCallback;

    public FocusClickArea(Runnable clickCallback, Widget child) {
        super(child);
        this.clickCallback = clickCallback;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<FocusClickArea> {
        public Instance(FocusClickArea widget) {
            super(widget);
        }
    }
}
