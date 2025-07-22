package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.action.ActionTrigger;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.collapsible.LazyCollapsible;
import io.wispforest.owo.util.EventSource;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Unit;
import org.lwjgl.glfw.GLFW;

public class CollapsibleEntry extends StatefulWidget {

    public final EventSource<BraidEventStream.Listener<Unit>> onExpand;
    public final boolean startCollapsed;
    public final Widget title;
    public final Widget content;

    public CollapsibleEntry(EventSource<BraidEventStream.Listener<Unit>> onExpand, boolean startCollapsed, Widget title, Widget content) {
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
            return new Actions(
                widget -> widget
                    .addAction(EXPAND_TRIGGER, () -> this.setState(() -> this.collapsed = false))
                    .addAction(COLLAPSE_TRIGGER, () -> this.setState(() -> this.collapsed = true)),
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

    private static final ActionTrigger EXPAND_TRIGGER = new ActionTrigger(null, IntSet.of(GLFW.GLFW_KEY_RIGHT), null);
    private static final ActionTrigger COLLAPSE_TRIGGER = new ActionTrigger(null, IntSet.of(GLFW.GLFW_KEY_LEFT), null);
}
