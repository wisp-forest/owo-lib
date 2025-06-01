package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class Builder extends StatelessWidget {
    public final WidgetBuilder builder;

    public Builder(WidgetBuilder builder) {
        this.builder = builder;
    }

    @Override
    public Widget build(BuildContext context) {
        return this.builder.build(context);
    }

    @FunctionalInterface
    public interface WidgetBuilder {
        Widget build(BuildContext context);
    }
}
