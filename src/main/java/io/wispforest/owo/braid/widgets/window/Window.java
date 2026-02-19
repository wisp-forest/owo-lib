package io.wispforest.owo.braid.widgets.window;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
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
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Window extends StatefulWidget {

    public final boolean collapsible;
    public final Component title;
    public final @Nullable Runnable onClose;
    public final @Nullable WindowController controller;
    public final Size initialSize;
    public final Size minSize;
    public final Size maxSize;

    public final Widget content;

    public Window(boolean collapsible, Component title, @Nullable Runnable onClose, @Nullable WindowController controller, Size initialSize, Size minSize, Size maxSize, Widget content) {
        this.collapsible = collapsible;
        this.title = title;
        this.onClose = onClose;
        this.controller = controller;
        this.initialSize = initialSize;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.content = content;
    }

    public Window(boolean collapsible, Component title, @Nullable Runnable onClose, @Nullable WindowController controller, Size initialSize, Widget content) {
        this(collapsible, title, onClose, controller, initialSize, Size.square(40), Size.square(Double.POSITIVE_INFINITY), content);
    }

    @Override
    public WidgetState<Window> createState() {
        return new State();
    }

    public static class State extends WidgetState<Window> {

        private WindowController controller;
        private WindowController internalController;

        private Set<Edge> draggingEdges;
        private Size draggingSize;

        @Override
        public void init() {
            this.internalController = new WindowController();
            this.updateController();

            this.controller.setSize(this.widget().initialSize);
            this.applySize(this.widget().initialSize);
        }

        @Override
        public void didUpdateWidget(Window oldWidget) {
            this.updateController();
            this.applySize(this.controller.size());
        }

        private void updateController() {
            this.controller = this.widget().controller != null ? this.widget().controller : this.internalController;
        }

        @Override
        public Widget build(BuildContext context) {
            return new ListenableBuilder(
                this.controller,
                (buildContext, child) -> {
                    var titleBar = new ArrayList<Widget>();
                    if (this.widget().collapsible) {
                        titleBar.add(Interactable.primary(
                            () -> this.controller.toggleCollapsed(),
                            new Padding(
                                Insets.of(2, 0, 0, 4),
                                new Label(Component.literal(this.controller.collapsed() ? "⏶" : "⏷"))
                            )
                        ));
                    }

                    titleBar.add(new Flexible(new Label(new LabelStyle(Alignment.LEFT, null, null, null), false, Label.Overflow.ELLIPSIS, this.widget().title)));

                    if (this.widget().onClose != null) {
                        titleBar.add(Interactable.primary(
                            () -> this.widget().onClose.run(),
                            new HoverStyledLabel(Component.literal("x"), Style.EMPTY.applyFormat(ChatFormatting.RED))
                        ));
                    }

                    return new DragArenaElement(
                        Math.ceil(this.controller.x()),
                        Math.ceil(this.controller.y()),
                        new MouseArea(
                            widget -> widget
                                //TODO: decide what to do with buttons here
                                .clickCallback((x, y, button, modifiers) -> {
                                    if (button != 0) return false;
                                    this.draggingEdges = this.edgesAt(x, y);
                                    this.draggingSize = this.controller.size();
                                    return true;
                                })
                                .dragCallback((x, y, dx, dy) -> this.resize(dx, dy))
                                .dragEndCallback(() -> {
                                    this.draggingEdges = null;
                                    this.draggingSize = null;
                                })
                                .cursorStyleSupplier((x, y) -> this.cursorStyleFor(this.edgesAt(x, y))),
                            new Padding(
                                Insets.all(4),
                                new HitTestTrap(
                                    new MouseArea(
                                        widget -> widget
                                            .dragCallback((x, y, dx, dy) -> {
                                                this.controller.setX(this.controller.x() + dx);
                                                this.controller.setY(this.controller.y() + dy);
                                            }),
                                        new Sized(
                                            Size.of(
                                                this.controller.size().width(),
                                                this.controller.size().height() + 15
                                            ).floor(),
                                            new Column(
                                                new Sized(
                                                    null,
                                                    15.0,
                                                    new Box(
                                                        Color.BLACK.withA(.75),
                                                        new Padding(
                                                            Insets.horizontal(4),
                                                            new Row(titleBar)
                                                        )
                                                    )
                                                ),
                                                new Flexible(
                                                    new Visibility(
                                                        !this.controller.collapsed(),
                                                        false,
                                                        child
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    );
                },
                new Box(
                    Color.BLACK.withA(.65),
                    new Padding(
                        Insets.all(4),
                        new Clip(
                            this.widget().content
                        )
                    )
                )
            );
        }

        protected Set<Edge> edgesAt(double x, double y) {
            var result = new HashSet<Edge>();

            if (y < 4) result.add(Edge.TOP);
            if (y > this.controller.size().height() + 4 + 15) result.add(Edge.BOTTOM);

            if (x < 4) result.add(Edge.LEFT);
            if (x > this.controller.size().width() + 4) result.add(Edge.RIGHT);

            return result;
        }

        protected void resize(double dx, double dy) {
            var size = this.draggingSize;

            if (this.draggingEdges.contains(Edge.TOP)) {
                size = size.with(null, size.height() - dy);
                this.controller.setY(this.controller.y() + dy);
            } else if (this.draggingEdges.contains(Edge.BOTTOM)) {
                size = size.with(null, size.height() + dy);
            }

            if (this.draggingEdges.contains(Edge.LEFT)) {
                size = size.with(size.width() - dx, null);
                this.controller.setX(this.controller.x() + dx);
            } else if (this.draggingEdges.contains(Edge.RIGHT)) {
                size = size.with(size.width() + dx, null);
            }

            this.draggingSize = size;
            this.applySize(this.draggingSize);
        }

        private void applySize(Size size) {
            this.controller.setSize(Size.of(
                Mth.clamp(size.width(), this.widget().minSize.width(), this.widget().maxSize.width()),
                Mth.clamp(size.height(), this.widget().minSize.height(), this.widget().maxSize.height())
            ));
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
