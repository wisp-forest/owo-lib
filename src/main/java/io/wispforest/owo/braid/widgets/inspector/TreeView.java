package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.animated.AnimatedBox;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.collapsible.LazyCollapsible;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventSource;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.intents.Intent;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.intents.ShortcutTrigger;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class TreeView extends StatefulWidget {

    public final InspectorTreeNode node;
    private final int parentChildCount;

    public TreeView(InspectorTreeNode node) {
        this(node, 0);
    }

    private TreeView(InspectorTreeNode node, int parentChildCount) {
        this.node = node;
        this.parentChildCount = parentChildCount;
    }

    @Override
    public WidgetState<TreeView> createState() {
        return new State();
    }

    // ---

    static class State extends WidgetState<TreeView> {

        private boolean collapsed;
        private boolean highlight = false;
        private boolean branchHovered = false;
        private @Nullable BraidEventSource<RevealEvent>.Subscription revealSubscription;

        @Override
        public void init() {
            var inspectorState = SharedState.getWithoutDependency(this.context(), InspectorState.class);
            var initialRevealEvent = inspectorState.lastRevealEvent;
            this.collapsed = initialRevealEvent == null || !initialRevealEvent.path().contains(this.widget().node.instance());

            if (initialRevealEvent != null && initialRevealEvent.target() == this.widget().node.instance()) {
                this.schedulePostLayoutCallback(() -> Scrollable.reveal(this.context(), Insets.all(20)));
            }

            if (inspectorState.revealSource != null) {
                this.revealSubscription = inspectorState.revealSource.subscribe(event -> this.setState(() -> {
                    if (event.path().contains(this.widget().node.instance())) this.collapsed = false;
                    if (event.target() == this.widget().node.instance()) this.schedulePostLayoutCallback(() -> Scrollable.reveal(this.context(), Insets.all(20)));
                }));
            }
        }

        @Override
        public void dispose() {
            if (this.revealSubscription != null) this.revealSubscription.cancel();
        }

        @Override
        public void didUpdateWidget(TreeView oldWidget) {
            if (!oldWidget.node.equals(this.widget().node)) this.highlight = true;
        }

        private Widget buildCollapsible(Widget title, Widget content) {
            return new Interactable(
                SHORTCUTS,
                w -> w.addCallbackAction(SetCollapsedIntent.class, (ignored, intent) -> this.setState(() -> this.collapsed = intent.collapsed())),
                new LazyCollapsible(
                    false,
                    this.collapsed,
                    nowCollapsed -> this.setState(() -> this.collapsed = nowCollapsed),
                    new MouseArea(
                        w -> w
                            .enterCallback(() -> this.setState(() -> this.branchHovered = true))
                            .exitCallback(() -> this.setState(() -> this.branchHovered = false)),
                        title
                    ),
                    content
                )
            );
        }

        @Override
        public Widget build(BuildContext context) {
            var tree = this.widget();

            if (this.highlight) this.schedulePostLayoutCallback(() -> this.setState(() -> this.highlight = false));

            var children = tree.node.children();
            var title = tree.node.buildTitle();

            Widget entry;

            if (children.isEmpty()) {
                entry = new Row(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Sized(12, 12, new SpriteWidget(Owo.id("braid_inspector_leaf"))),
                    title
                );
            } else if (children.size() == 1) {
                if (tree.parentChildCount == 1) {
                    entry = new Column(
                        new Row(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Sized(12, 12, new SpriteWidget(Owo.id("braid_collapsible_open_disabled"))),
                            title
                        ),
                        new TreeView(children.getFirst(), 1)
                    );
                } else {
                    entry = buildCollapsible(title, new TreeView(children.getFirst(), 1));
                }
            } else {
                var lineColor = this.branchHovered ? Color.WHITE : Color.mix(.5f, Color.WHITE, Color.BLACK);
                entry = buildCollapsible(title, new Column(
                    IntStream.range(0, children.size())
                        .mapToObj(i -> new Stack(
                            new Align(
                                Alignment.TOP_LEFT,
                                new Padding(
                                    Insets.left(6),
                                    new Sized(1, i == children.size() - 1 ? 7 : Double.POSITIVE_INFINITY, new Box(lineColor))
                                )
                            ),
                            new Align(
                                Alignment.TOP_LEFT,
                                new Padding(
                                    Insets.top(6).withLeft(6),
                                    new Sized(5, 1, new Box(lineColor))
                                )
                            ),
                            new Align(
                                Alignment.TOP_LEFT,
                                new MouseArea(
                                    w -> w
                                        .enterCallback(() -> this.setState(() -> this.branchHovered = true))
                                        .exitCallback(() -> this.setState(() -> this.branchHovered = false)),
                                    new Sized(10, Double.POSITIVE_INFINITY, EmptyWidget.INSTANCE)
                                )
                            ),
                            new StackBase(
                                new Padding(
                                    Insets.left(10),
                                    new TreeView(children.get(i), children.size())
                                )
                            )
                        )).toList()
                ));
            }

            return new AnimatedBox(
                this.highlight ? Duration.ZERO : Duration.ofMillis(1250),
                Easing.IN_OUT_SINE,
                this.highlight ? Color.hsv((tree.node.depth() % 15) / 15d, .75, 1, .5) : new Color(0),
                true,
                entry
            );
        }
    }

    // ---

    record SetCollapsedIntent(boolean collapsed) implements Intent {}

    static final Map<List<ShortcutTrigger>, Intent> SHORTCUTS = Map.of(
        List.of(ShortcutTrigger.LEFT), new SetCollapsedIntent(true),
        List.of(ShortcutTrigger.RIGHT), new SetCollapsedIntent(false)
    );
}
