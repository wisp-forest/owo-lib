package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoverableBuilder extends StatefulWidget {
    public final HoverableBuilderCallback builder;
    public final @Nullable Widget child;

    public HoverableBuilder(HoverableBuilderCallback builder, @NotNull Widget child) {
        this.builder = builder;
        this.child = child;
    }

    public HoverableBuilder(HoverableBuilderCallbackWithoutChild builder) {
        this.builder = (context, hovered, $) -> builder.build(context, hovered);
        this.child = null;
    }

    public HoverableBuilder(Widget notHovered, Widget hovered) {
        this((context, isHovered) -> isHovered ? hovered : notHovered);
    }

    @Override
    public WidgetState<HoverableBuilder> createState() {
        return new State();
    }

    public static class State extends WidgetState<HoverableBuilder> {

        private boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false)),
                this.widget().builder.build(context, this.hovered, this.widget().child)
            );
        }
    }

    @FunctionalInterface
    public interface HoverableBuilderCallback {
        Widget build(BuildContext hoverableContext, boolean hovered, Widget child);
    }

    @FunctionalInterface
    public interface HoverableBuilderCallbackWithoutChild {
        Widget build(BuildContext hoverableContext, boolean hovered);
    }
}
