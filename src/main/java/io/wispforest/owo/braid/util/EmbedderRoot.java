package io.wispforest.owo.braid.util;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

import java.util.function.Consumer;

public class EmbedderRoot extends SingleChildInstanceWidget {

    public final Consumer<Instance> instanceListener;

    public EmbedderRoot(Consumer<Instance> instanceListener, Widget child) {
        super(child);
        this.instanceListener = instanceListener;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        var instance = new Instance(this);
        this.instanceListener.accept(instance);

        return instance;
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<EmbedderRoot> {
        public Instance(EmbedderRoot widget) {
            super(widget);
        }
    }
}