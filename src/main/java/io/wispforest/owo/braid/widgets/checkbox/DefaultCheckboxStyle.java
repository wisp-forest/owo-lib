package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import org.jetbrains.annotations.Nullable;

public class DefaultCheckboxStyle extends InheritedWidget {

    public final CheckboxStyle style;

    public DefaultCheckboxStyle(CheckboxStyle style, Widget child) {
        super(child);
        this.style = style;
    }

    public static Widget merge(CheckboxStyle style, Widget child) {
        return new Builder(context -> {
            var contextStyle = DefaultCheckboxStyle.maybeOf(context);
            return new DefaultCheckboxStyle(contextStyle != null ? style.overriding(contextStyle) : style, child);
        });
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return !this.style.equals(((DefaultCheckboxStyle) newWidget).style);
    }

    public static @Nullable CheckboxStyle maybeOf(BuildContext context) {
        var widget = context.dependOnAncestor(DefaultCheckboxStyle.class);
        if (widget != null) {
            return widget.style;
        } else {
            return null;
        }
    }
}