package io.wispforest.owo.braid.widgets.window;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.HoverStyledLabel;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Window extends StatefulWidget {

    public final boolean collapsible;
    public final Text title;
    public final @Nullable Runnable onClose;
    public final @Nullable WindowController controller;

    public final Widget content;

    public Window(boolean collapsible, Text title, @Nullable Runnable onClose, @Nullable WindowController controller, Widget content) {
        this.collapsible = collapsible;
        this.title = title;
        this.onClose = onClose;
        this.controller = controller;
        this.content = content;
    }

    @Override
    public WidgetState<Window> createState() {
        return new State();
    }

    public static class State extends WidgetState<Window> {

        private WindowController controller;
        private @Nullable Set<Edge> draggingEdges;

        @Override
        public void init() {
            super.init();
            this.controller = this.widget().controller != null
                ? this.widget().controller
                : new WindowController(Size.of(100, 75));
        }

        @Override
        public void didUpdateWidget(Window oldWidget) {
            super.didUpdateWidget(oldWidget);
            if (this.widget().controller != null) {
                this.controller = this.widget().controller;
            }
        }

        @Override
        public Widget build(BuildContext context) {
            var titleBar = new ArrayList<Widget>();
            if (this.widget().collapsible) {
                titleBar.add(new MouseArea(
                    widget -> widget
                        .clickCallback((x, y) -> this.setState(() -> this.controller.expanded = !this.controller.expanded))
                        .cursorStyle(CursorStyle.HAND),
                    new Padding(
                        Insets.of(2, 0, 0, 4),
                        new Label(Text.literal(this.controller.expanded ? "⏷" : "⏶"))
                    )
                ));
            }

            titleBar.add(new Label(this.widget().title));
            titleBar.add(new Flexible(new Padding(Insets.none())));

            if (this.widget().onClose != null) {
                titleBar.add(new MouseArea(
                    widget -> widget
                        .clickCallback((x, y) -> this.widget().onClose.run())
                        .cursorStyle(CursorStyle.HAND),
                    new HoverStyledLabel(Text.literal("x"), Style.EMPTY.withFormatting(Formatting.RED))
                ));
            }

            return new DragArenaElement(
                Math.ceil(this.controller.x),
                Math.ceil(this.controller.y),
                new MouseArea(
                    widget -> widget
                        .clickCallback((x, y) -> this.draggingEdges = this.edgesAt(x, y))
                        .dragCallback((x, y, dx, dy) -> setState(() -> this.resize(dx, dy)))
                        .dragEndCallback(() -> this.draggingEdges = null)
                        .cursorStyleSupplier((x, y) -> this.cursorStyleFor(this.edgesAt(x, y))),
                    new Padding(
                        Insets.all(4),
                        new HitTestTrap(
                            new MouseArea(
                                widget -> widget
                                    .dragCallback((x, y, dx, dy) -> this.setState(() -> {
                                        this.controller.x += dx;
                                        this.controller.y += dy;
                                    })),
                                new Column(
                                    new Sized(
                                        Math.floor(this.controller.size.width()),
                                        15.0,
                                        new Box(
                                            Color.BLACK.interpolate(Color.ofArgb(0), .25f),
                                            new Padding(
                                                Insets.horizontal(4),
                                                new Row(titleBar)
                                            )
                                        )
                                    ),
                                    new Visibility(
                                        this.controller.expanded,
                                        false,
                                        new Box(
                                            Color.BLACK.interpolate(Color.ofArgb(0), .35f),
                                            new Sized(
                                                Math.floor(this.controller.size.width()),
                                                Math.floor(this.controller.size.height()),
                                                new Clip(
                                                    new Padding(
                                                        Insets.all(4),
                                                        this.widget().content
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            );
        }

        protected Set<Edge> edgesAt(double x, double y) {
            var result = new HashSet<Edge>();

            if (y < 4) result.add(Edge.TOP);
            if (y > controller.size.height() + 4 + 15) result.add(Edge.BOTTOM);

            if (x < 4) result.add(Edge.LEFT);
            if (x > controller.size.width() + 4) result.add(Edge.RIGHT);

            return result;
        }

        protected void resize(double dx, double dy) {
            if (this.draggingEdges.contains(Edge.TOP)) {
                this.controller.size = this.controller.size.with(null, this.controller.size.height() - dy);
                this.controller.y += dy;
            } else if (this.draggingEdges.contains(Edge.BOTTOM)) {
                this.controller.size = this.controller.size.with(null, this.controller.size.height() + dy);
            }

            if (this.draggingEdges.contains(Edge.LEFT)) {
                this.controller.size = this.controller.size.with(this.controller.size.width() - dx, null);
                this.controller.x += dx;
            } else if (this.draggingEdges.contains(Edge.RIGHT)) {
                this.controller.size = this.controller.size.with(this.controller.size.width() + dx, null);
            }
        }

        protected @Nullable CursorStyle cursorStyleFor(Set<Edge> edges) {
            if (edges.size() == 1) {
                if (edges.contains(Edge.TOP) || edges.contains(Edge.BOTTOM)) return CursorStyle.VERTICAL_RESIZE;
                if (edges.contains(Edge.LEFT) || edges.contains(Edge.RIGHT)) return CursorStyle.HORIZONTAL_RESIZE;
            } else if (edges.size() == 2) {
                if ((edges.contains(Edge.TOP) && edges.contains(Edge.LEFT)) || (edges.contains(Edge.BOTTOM) && edges.contains(Edge.RIGHT))) {
                    return CursorStyle.NWSE_RESIZE;
                }

                if ((edges.contains(Edge.BOTTOM) && edges.contains(Edge.LEFT)) || (edges.contains(Edge.TOP) && edges.contains(Edge.RIGHT))) {
                    return CursorStyle.NESW_RESIZE;
                }
            }

            return null;
        }
    }

    protected enum Edge {
        TOP, LEFT, RIGHT, BOTTOM
    }
}
