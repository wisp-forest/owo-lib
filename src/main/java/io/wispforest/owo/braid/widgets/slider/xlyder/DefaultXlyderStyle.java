package io.wispforest.owo.braid.widgets.slider.xlyder;

import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.slider.SliderStyle;
import org.jetbrains.annotations.Nullable;

public class DefaultXlyderStyle extends InheritedWidget {

    public final SliderStyle<Size> style;

    public DefaultXlyderStyle(SliderStyle<Size> style, Widget child) {
        super(child);
        this.style = style;
    }

    public static Widget merge(SliderStyle<Size> style, Widget child) {
        return new Builder(context -> {
            var contextStyle = DefaultXlyderStyle.maybeOf(context);
            return new DefaultXlyderStyle(contextStyle != null ? style.overriding(contextStyle) : style, child);
        });
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return !this.style.equals(((DefaultXlyderStyle) newWidget).style);
    }

    public static @Nullable SliderStyle<Size> maybeOf(BuildContext context) {
        var widget = context.dependOnAncestor(DefaultXlyderStyle.class);
        if (widget != null) {
            return widget.style;
        } else {
            return null;
        }
    }
}