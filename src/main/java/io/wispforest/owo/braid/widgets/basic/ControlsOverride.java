package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

/// A widget that descendants can check to disable interactive controls,
/// such as buttons or text fields.
///
/// This is useful for deactivating larger sections of a UI
/// without having to manually disable each individual widget.
public class ControlsOverride extends InheritedWidget {

    public final boolean disableControls;

    public ControlsOverride(boolean disableControls, Widget child) {
        super(child);
        this.disableControls = disableControls;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return this.disableControls != ((ControlsOverride) newWidget).disableControls;
    }

    public static boolean controlsDisabled(BuildContext context) {
        var widget = context.dependOnAncestor(ControlsOverride.class);
        return widget != null && widget.disableControls;
    }
}

//TODO: make sure this is applied to all relevant widgets
