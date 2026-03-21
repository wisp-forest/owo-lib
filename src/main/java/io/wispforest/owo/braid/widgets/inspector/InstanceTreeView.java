package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.animation.Easing;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.animated.AnimatedBox;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventSource;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventStream;
import io.wispforest.owo.braid.widgets.eventstream.StreamListenerState;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import net.minecraft.util.Unit;

import java.time.Duration;
import java.util.ArrayList;

public class InstanceTreeView extends StatefulWidget {

    public final BraidEventSource<RevealInstanceEvent> revealEvents;
    public final WidgetInstance<?> viewInstance;
    public final int parentChildCount;

    public InstanceTreeView(BraidEventSource<RevealInstanceEvent> revealEvents, WidgetInstance<?> viewInstance) {
        this(revealEvents, viewInstance, 0);
    }

    public InstanceTreeView(BraidEventSource<RevealInstanceEvent> revealEvents, WidgetInstance<?> viewInstance, int parentChildCount) {
        this.revealEvents = revealEvents;
        this.viewInstance = viewInstance;
        this.parentChildCount = parentChildCount;
    }

    @Override
    public WidgetState<InstanceTreeView> createState() {
        return new State();
    }

    public static class State extends StreamListenerState<InstanceTreeView> {

        public final BraidEventStream<Unit> expandEvents = new BraidEventStream<>();

        public boolean builtOnce = false;
        public boolean highlight = false;

        private void reveal() {
            this.schedulePostLayoutCallback(() -> {
                Scrollable.reveal(this.context(), Insets.all(20));
            });
        }

        @Override
        public void init() {
            this.streamListen(
                widget -> widget.revealEvents,
                event -> {
                    if (event.instance == this.widget().viewInstance) {
                        this.reveal();
                    }

                    if (event.fullPath.contains(this.widget().viewInstance)) {
                        this.expandEvents.sink().onEvent(Unit.INSTANCE);
                    }
                }
            );
        }

        @Override
        public void didUpdateWidget(InstanceTreeView oldWidget) {
            if (oldWidget.viewInstance != this.widget().viewInstance) {
                this.setState(() -> {
                    this.highlight = true;
                });
            }
        }

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            var title = new InstanceTitle(widget.viewInstance);

            var children = new ArrayList<WidgetInstance<?>>();
            widget.viewInstance.visitChildren(children::add);

            if (this.highlight) {
                this.schedulePostLayoutCallback(() -> this.setState(() -> this.highlight = false));
            }

            var startCollapsed = true;
            if (!this.builtOnce) {
                this.builtOnce = true;

                var lastRevealEvent = SharedState.getWithoutDependency(context, InspectorState.class).lastRevealEvent;
                if (lastRevealEvent != null && lastRevealEvent.instance == widget.viewInstance) {
                    this.reveal();
                }

                startCollapsed = lastRevealEvent == null || !lastRevealEvent.fullPath.contains(widget.viewInstance);
            }

            Widget entry;
            if (!children.isEmpty()) {
                var collapsibleEntry = new CollapsibleEntry(
                    this.expandEvents.source(),
                    startCollapsed,
                    widget.parentChildCount,
                    title,
                    children.stream()
                        .map(child -> new InstanceTreeView(widget.revealEvents, child, children.size()))
                        .toList()
                );
                entry = collapsibleEntry;
            } else {
                entry = new Row(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Sized(12, 12, new SpriteWidget(Owo.id("braid_inspector_leaf"))),
                    title
                );
            }

            return new AnimatedBox(
                this.highlight ? Duration.ZERO : Duration.ofMillis(1250),
                Easing.IN_OUT_SINE,
                this.highlight ? Color.hsv((widget.viewInstance.depth() % 15) / 15d, .75, 1, .5) : new Color(0),
                true,
                entry
            );
        }
    }
}
