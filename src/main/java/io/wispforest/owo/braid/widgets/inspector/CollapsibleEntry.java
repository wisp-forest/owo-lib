package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.collapsible.LazyCollapsible;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventSource;
import io.wispforest.owo.braid.widgets.eventstream.StreamListenerState;
import io.wispforest.owo.braid.widgets.intents.Intent;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.intents.ShortcutTrigger;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.Map;

public class CollapsibleEntry extends StatefulWidget {

    public final BraidEventSource<Unit> onExpand;
    public final boolean startCollapsed;
    public final Widget title;
    public final Widget content;

    public CollapsibleEntry(BraidEventSource<Unit> onExpand, boolean startCollapsed, Widget title, Widget content) {
        this.onExpand = onExpand;
        this.startCollapsed = startCollapsed;
        this.title = title;
        this.content = content;
    }

    @Override
    public WidgetState<CollapsibleEntry> createState() {
        return new State();
    }

    public static class State extends StreamListenerState<CollapsibleEntry> {

        private boolean collapsed;

        private void expand(Unit unit) {
            this.setState(() -> {
                this.collapsed = false;
            });
        }

        @Override
        public void init() {
            this.streamListen(widget -> widget.onExpand, this::expand);
            this.collapsed = this.widget().startCollapsed;
        }

        @Override
        public Widget build(BuildContext context) {
            return new Interactable(
                SHORTCUTS,
                widget -> widget
                    .addCallbackAction(SetCollapsedIntent.class, (actionCtx, intent) -> {
                        this.setState(() -> this.collapsed = intent.collapsed());
                    }),
                new LazyCollapsible(
                    true,
                    this.collapsed,
                    nowCollapsed -> this.setState(() -> this.collapsed = nowCollapsed),
                    this.widget().title,
                    this.widget().content
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