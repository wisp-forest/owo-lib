package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class Hoverable extends StatefulWidget {
    public final Widget notHovered;
    public final Widget hovered;

    public Hoverable(Widget notHovered, Widget hovered) {
        this.notHovered = notHovered;
        this.hovered = hovered;
    }

    public Hoverable(HoverableBuilder builder) {
        this(builder.build(false), builder.build(true));
    }

    @Override
    public WidgetState<Hoverable> createState() {
        return new State();
    }

    public static class State extends WidgetState<Hoverable> {

        private boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false)),
                this.hovered ? this.widget().hovered : this.widget().notHovered
            );
        }
    }

    @FunctionalInterface
    public interface HoverableBuilder {
        Widget build(boolean hovered);
    }
}
