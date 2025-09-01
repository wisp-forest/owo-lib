package io.wispforest.owo.braid.widgets.overlay;

import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

class OverlayProvider extends InheritedWidget {

    public final Overlay.State state;

    protected OverlayProvider(Overlay.State state, Widget child) {
        super(child);
        this.state = state;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return false;
    }
}
