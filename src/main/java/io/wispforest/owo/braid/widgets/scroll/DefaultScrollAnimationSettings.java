package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class DefaultScrollAnimationSettings extends InheritedWidget {

    public final ScrollAnimationSettings settings;

    public DefaultScrollAnimationSettings(ScrollAnimationSettings settings, Widget child) {
        super(child);
        this.settings = settings;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return this.settings != ((DefaultScrollAnimationSettings) newWidget).settings;
    }

    // ---

    public static @Nullable ScrollAnimationSettings maybeOf(BuildContext context) {
        var widget = context.dependOnAncestor(DefaultScrollAnimationSettings.class);
        if (widget != null) {
            return widget.settings;
        } else {
            return null;
        }
    }
}
