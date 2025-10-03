package io.wispforest.owo.braid.widgets.focus;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class FocusPolicy extends InheritedWidget {

    public final boolean clickFocus;

    public FocusPolicy(boolean clickFocus, Widget child) {
        super(child);
        this.clickFocus = clickFocus;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return ((FocusPolicy) newWidget).clickFocus != this.clickFocus;
    }

    // ---

    public static FocusPolicy of(BuildContext context) {
        return context.dependOnAncestor(FocusPolicy.class);
    }
}
