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
import io.wispforest.owo.braid.widgets.eventstream.StreamListenerState;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.intents.Intent;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.intents.ShortcutTrigger;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

public class TreeView<T> extends StatefulWidget {

    public final T root;
    public final Function<T, List<T>> childrenProvider;
    public final Function<T, Widget> titleBuilder;
    public final @Nullable BraidEventSource<RevealEvent<?>> revealSource;
    private final @Nullable RevealEvent<?> initialRevealEvent;
    private final int depth;
    private final int parentChildCount;

    public TreeView(
        T root,
        Function<T, List<T>> childrenProvider,
        Function<T, Widget> titleBuilder,
        @Nullable BraidEventSource<RevealEvent<?>> revealSource,
        @Nullable RevealEvent<?> initialRevealEvent,
        int depth,
        int parentChildCount
    ) {
        this.root = root;
        this.childrenProvider = childrenProvider;
        this.titleBuilder = titleBuilder;
        this.revealSource = revealSource;
        this.initialRevealEvent = initialRevealEvent;
        this.depth = depth;
        this.parentChildCount = parentChildCount;
    }

    @Override
    public WidgetState<TreeView<T>> createState() {
        return new State<>();
    }

    static class State<T> extends StreamListenerState<TreeView<T>> {

        private boolean collapsed;
        private boolean highlight = false;
        private boolean revealHandled = false;
        private @Nullable RevealEvent<?> lastRevealEvent;

        @Override
        public void init() {
            this.lastRevealEvent = this.widget().initialRevealEvent;
            this.collapsed = this.lastRevealEvent == null || !this.lastRevealEvent.path().contains(this.widget().root);

            this.streamListen(
                w -> w.revealSource,
                event -> this.setState(() -> {
                    this.lastRevealEvent = event;
                    if (event.path().contains(this.widget().root)) this.collapsed = false;
                    this.revealHandled = false;
                })
            );
        }

        @Override
        public void didUpdateWidget(TreeView<T> oldWidget) {
            super.didUpdateWidget(oldWidget);
            if (oldWidget.root != this.widget().root) this.highlight = true;
        }

        private TreeView<T> child(T node, int parentChildCount) {
            var tree = this.widget();
            return new TreeView<>(node, tree.childrenProvider, tree.titleBuilder, tree.revealSource, this.lastRevealEvent, tree.depth + 1, parentChildCount);
        }

        @Override
        public Widget build(BuildContext context) {
            var tree = this.widget();

            if (this.highlight) {
                this.schedulePostLayoutCallback(() -> this.setState(() -> this.highlight = false));
            }

            var isRevealTarget = this.lastRevealEvent != null && this.lastRevealEvent.target() == tree.root;
            if (isRevealTarget && !this.revealHandled) {
                this.revealHandled = true;
                this.schedulePostLayoutCallback(() -> Scrollable.reveal(this.context(), Insets.all(20)));
            }

            var children = tree.childrenProvider.apply(tree.root);
            var title = tree.titleBuilder.apply(tree.root);

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
                        this.child(children.getFirst(), 1)
                    );
                } else {
                    entry = new SharedState<>(
                        BranchHoverState::new,
                        new Builder(ctx ->
                            new Interactable(
                                SHORTCUTS,
                                w -> w.addCallbackAction(SetCollapsedIntent.class, (ignored, intent) -> this.setState(() -> this.collapsed = intent.collapsed())),
                                new LazyCollapsible(
                                    false,
                                    this.collapsed,
                                    nowCollapsed -> this.setState(() -> this.collapsed = nowCollapsed),
                                    new MouseArea(
                                        w -> w
                                            .enterCallback(() -> SharedState.set(ctx, BranchHoverState.class, s -> s.hovered = true))
                                            .exitCallback(() -> SharedState.set(ctx, BranchHoverState.class, s -> s.hovered = false)),
                                        title
                                    ),
                                    this.child(children.getFirst(), 1)
                                )
                            )
                        )
                    );
                }
            } else {
                entry = new SharedState<>(
                    BranchHoverState::new,
                    new Builder(ctx -> {
                        var hovered = SharedState.select(ctx, BranchHoverState.class, s -> s.hovered);
                        var lineColor = hovered ? Color.WHITE : Color.mix(.5f, Color.WHITE, Color.BLACK);
                        return new Interactable(
                            SHORTCUTS,
                            w -> w.addCallbackAction(SetCollapsedIntent.class, (ignored, intent) -> this.setState(() -> this.collapsed = intent.collapsed())),
                            new LazyCollapsible(
                                false,
                                this.collapsed,
                                nowCollapsed -> this.setState(() -> this.collapsed = nowCollapsed),
                                new MouseArea(
                                    w -> w
                                        .enterCallback(() -> SharedState.set(ctx, BranchHoverState.class, s -> s.hovered = true))
                                        .exitCallback(() -> SharedState.set(ctx, BranchHoverState.class, s -> s.hovered = false)),
                                    title
                                ),
                                new Column(
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
                                                        .enterCallback(() -> SharedState.set(ctx, BranchHoverState.class, s -> s.hovered = true))
                                                        .exitCallback(() -> SharedState.set(ctx, BranchHoverState.class, s -> s.hovered = false)),
                                                    new Sized(10, null, EmptyWidget.INSTANCE)
                                                )
                                            ),
                                            new StackBase(
                                                new Padding(
                                                    Insets.left(10),
                                                    this.child(children.get(i), children.size())
                                                )
                                            )
                                        )).toList()
                                )
                            )
                        );
                    })
                );
            }

            return new AnimatedBox(
                this.highlight ? Duration.ZERO : Duration.ofMillis(1250),
                Easing.IN_OUT_SINE,
                this.highlight ? Color.hsv((tree.depth % 15) / 15d, .75, 1, .5) : new Color(0),
                true,
                entry
            );
        }
    }

    // ---

    public static class BranchHoverState extends ShareableState {
        public boolean hovered = false;
    }

    record SetCollapsedIntent(boolean collapsed) implements Intent {}

    static final Map<List<ShortcutTrigger>, Intent> SHORTCUTS = Map.of(
        List.of(ShortcutTrigger.LEFT), new SetCollapsedIntent(true),
        List.of(ShortcutTrigger.RIGHT), new SetCollapsedIntent(false)
    );
}
