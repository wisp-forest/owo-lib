package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

/// A Hoverable widget that displays [#hovered] when hovered, and [#notHovered] when not
public class Hoverable extends StatefulWidget {
    public final Widget notHovered;
    public final Widget hovered;

    public Hoverable(Widget notHovered, Widget hovered) {
        this.notHovered = notHovered;
        this.hovered = hovered;
    }

    /// Create a Hoverable widget using a [HoverableBuilder] to create both the hovered and not-hovered states.<br>
    /// Useful for creating `Hoverable`s that have similar structure in both states
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
            if (ControlsOverride.controlsDisabled(context)) return this.widget().notHovered;
            return new MouseArea(
                widget -> widget
                    .enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false)),
                this.hovered ? this.widget().hovered : this.widget().notHovered
            );
        }
    }

    /// Builder interface for creating Hoverable widgets.<br>
    /// The [#build] method is called twice, once with `hovered` set to `false`, and once with it set to `true`
    @FunctionalInterface
    public interface HoverableBuilder {
        Widget build(boolean hovered);
    }
}
