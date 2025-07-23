package io.wispforest.owo.braid.widgets.recipeviewer;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.util.ViewerStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class StackDropArea extends SingleChildInstanceWidget {
    public final Predicate<ViewerStack> stackPredicate;
    public final Consumer<ViewerStack> stackAcceptor;

    public StackDropArea(Predicate<ViewerStack> stackPredicate, Consumer<ViewerStack> stackAcceptor, Widget child) {
        super(child);
        this.stackPredicate = stackPredicate;
        this.stackAcceptor = stackAcceptor;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<StackDropArea> {
        public Instance(StackDropArea widget) {
            super(widget);
        }
    }
}
