package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class DefaultLabelStyle extends InheritedWidget {

    public final LabelStyle style;

    public DefaultLabelStyle(LabelStyle style, Widget child) {
        super(child);
        this.style = style;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return !this.style.equals(((DefaultLabelStyle) newWidget).style);
    }

    public static @Nullable LabelStyle maybeOf(BuildContext context) {
        var widget = context.dependOnAncestor(DefaultLabelStyle.class);
        if (widget != null) {
            return widget.style;
        } else {
            return null;
        }
    }
}
