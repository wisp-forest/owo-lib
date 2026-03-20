package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.collapsible.LazyCollapsible;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventSource;
import io.wispforest.owo.braid.widgets.eventstream.StreamListenerState;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.intents.Intent;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.intents.ShortcutTrigger;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class CollapsibleEntry extends StatefulWidget {

    public final BraidEventSource<Unit> onExpand;
    public final boolean startCollapsed;
    public final Widget title;
    public final List<? extends Widget> children;

    public CollapsibleEntry(BraidEventSource<Unit> onExpand, boolean startCollapsed, Widget title, List<? extends Widget> children) {
        this.onExpand = onExpand;
        this.startCollapsed = startCollapsed;
        this.title = title;
        this.children = children;
    }

    @Override
    public WidgetState<CollapsibleEntry> createState() {
        return new State();
    }

    public static class State extends StreamListenerState<CollapsibleEntry> {

        private boolean collapsed;
        private boolean hovered;

        @Override
        public void init() {
            this.streamListen(widget -> widget.onExpand, unit -> this.setState(() -> this.collapsed = false));
            this.collapsed = this.widget().startCollapsed;
        }

        @Override
        public Widget build(BuildContext context) {
            var children = this.widget().children;
            var single = children.size() == 1;
            var lineColor = this.hovered ? Color.WHITE : Color.mix(.5f, Color.WHITE, Color.BLACK);

            return new Interactable(
                SHORTCUTS,
                widget -> widget.addCallbackAction(
                    SetCollapsedIntent.class, (actionCtx, intent) -> {
                        this.setState(() -> this.collapsed = intent.collapsed());
                    }
                ),
                new LazyCollapsible(
                    false,
                    this.collapsed,
                    nowCollapsed -> this.setState(() -> this.collapsed = nowCollapsed),
                    new MouseArea(
                        w -> w.enterCallback(() -> this.setState(() -> this.hovered = true))
                            .exitCallback(() -> this.setState(() -> this.hovered = false)),
                        this.widget().title
                    ),
                    single
                        ? children.getFirst()
                        : new Column(
                            IntStream.range(0, children.size())
                                .mapToObj(i -> new Stack(
                                    new Align(
                                        Alignment.TOP_LEFT,
                                        new Padding(Insets.left(5), new Sized(1, i == children.size() - 1 ? 7 : Double.POSITIVE_INFINITY, new Box(lineColor)))
                                    ),
                                    new Align(
                                        Alignment.TOP_LEFT,
                                        new Padding(Insets.top(6).withLeft(5), new Sized(5, 1, new Box(lineColor)))
                                    ),
                                    new Align(
                                        Alignment.TOP_LEFT,
                                        new MouseArea(
                                            w1 -> w1.enterCallback(() -> this.setState(() -> this.hovered = true))
                                                .exitCallback(() -> this.setState(() -> this.hovered = false)),
                                            new Sized(10, Double.POSITIVE_INFINITY, new Padding(Insets.none()))
                                        )
                                    ),
                                    new StackBase(new Padding(Insets.left(10), children.get(i)))
                                )).toList()
                        )
                )
            );
        }
    }

    // ---

    public static final Map<List<ShortcutTrigger>, Intent> SHORTCUTS = Map.of(
        List.of(ShortcutTrigger.LEFT), new SetCollapsedIntent(true),
        List.of(ShortcutTrigger.RIGHT), new SetCollapsedIntent(false)
    );
}

record SetCollapsedIntent(boolean collapsed) implements Intent {}
