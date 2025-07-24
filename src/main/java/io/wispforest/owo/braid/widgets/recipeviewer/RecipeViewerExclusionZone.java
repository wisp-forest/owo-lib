package io.wispforest.owo.braid.widgets.recipeviewer;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class RecipeViewerExclusionZone extends SingleChildInstanceWidget {
    public RecipeViewerExclusionZone(Widget child) {
        super(child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<RecipeViewerExclusionZone> {
        public Instance(RecipeViewerExclusionZone widget) {
            super(widget);
        }
    }
}
