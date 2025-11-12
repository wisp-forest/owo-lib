package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import org.jetbrains.annotations.Nullable;

public class DefaultButtonStyle extends InheritedWidget {

    public final ButtonStyle style;

    public DefaultButtonStyle(ButtonStyle style, Widget child) {
        super(child);
        this.style = style;
    }

    public static Widget merge(ButtonStyle style, Widget child) {
        return new Builder(context -> {
            var contextStyle = DefaultButtonStyle.maybeOf(context);
            return new DefaultButtonStyle(contextStyle != null ? style.overriding(contextStyle) : style, child);
        });
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return !this.style.equals(((DefaultButtonStyle) newWidget).style);
    }

    public static @Nullable ButtonStyle maybeOf(BuildContext context) {
        var widget = context.dependOnAncestor(DefaultButtonStyle.class);
        if (widget != null) {
            return widget.style;
        } else {
            return null;
        }
    }
}