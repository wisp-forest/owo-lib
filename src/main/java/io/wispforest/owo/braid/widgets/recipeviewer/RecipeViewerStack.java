package io.wispforest.owo.braid.widgets.recipeviewer;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.util.ViewerStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class RecipeViewerStack extends SingleChildInstanceWidget {
    private final Supplier<ViewerStack> stackProvider;

    public RecipeViewerStack(Supplier<ViewerStack> stackProvider, Widget child) {
        super(child);
        this.stackProvider = stackProvider;
    }

    @ApiStatus.Internal
    public Supplier<ViewerStack> stackProvider() {
        return stackProvider;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<RecipeViewerStack> {
        public Instance(RecipeViewerStack widget) {
            super(widget);
        }
    }
}
