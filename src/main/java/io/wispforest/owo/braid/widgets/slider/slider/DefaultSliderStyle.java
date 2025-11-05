package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.slider.SliderStyle;
import org.jetbrains.annotations.Nullable;

public class DefaultSliderStyle extends InheritedWidget {

    public final SliderStyle<Double> style;

    public DefaultSliderStyle(SliderStyle<Double> style, Widget child) {
        super(child);
        this.style = style;
    }

    public static Widget merge(SliderStyle<Double> style, Widget child) {
        return new Builder(context -> {
            var contextStyle = DefaultSliderStyle.maybeOf(context);
            return new DefaultSliderStyle(contextStyle != null ? style.overriding(contextStyle) : style, child);
        });
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return !this.style.equals(((DefaultSliderStyle) newWidget).style);
    }

    public static @Nullable SliderStyle<Double> maybeOf(BuildContext context) {
        var widget = context.dependOnAncestor(DefaultSliderStyle.class);
        if (widget != null) {
            return widget.style;
        } else {
            return null;
        }
    }
}