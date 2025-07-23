package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.util.EventSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.ArrayList;

public class InstanceTreeView extends StatefulWidget {

    public final EventSource<BraidEventStream.Listener<RevealInstanceEvent>> revealEvents;
    public final WidgetInstance<?> viewInstance;

    public InstanceTreeView(EventSource<BraidEventStream.Listener<RevealInstanceEvent>> revealEvents, WidgetInstance<?> viewInstance) {
        this.revealEvents = revealEvents;
        this.viewInstance = viewInstance;
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
            var title = new InstanceTitle(this.widget().viewInstance);

            var children = new ArrayList<WidgetInstance<?>>();
            this.widget().viewInstance.visitChildren(children::add);

            if (this.highlight) {
                this.schedulePostLayoutCallback(() -> this.setState(() -> this.highlight = false));
            }

            var startCollapsed = true;
            if (!this.builtOnce) {
                this.builtOnce = true;

                var lastRevealEvent = SharedState.getWithoutDependency(context, InspectorState.class).lastRevealEvent;
                if (lastRevealEvent != null && lastRevealEvent.instance == this.widget().viewInstance) {
                    this.reveal();
                }

                startCollapsed = lastRevealEvent == null || !lastRevealEvent.fullPath.contains(this.widget().viewInstance);
            }

            // TODO: animated box
            return new Box(
                this.highlight ? Color.ofHsv((this.widget().viewInstance.depth() % 15) / 15f, .75f, 1, .5f) : Color.ofArgb(0),
                !children.isEmpty()
                    ?
                    new CollapsibleEntry(
                        this.expandEvents.source(),
                        startCollapsed,
                        title,
                        new Column(
                            children.stream()
                                .map(child -> new InstanceTreeView(this.widget().revealEvents, child))
                                .toList()
                        )
                    )
                    :
                        new Row(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Sized(
                                12,
                                12,
                                new SpriteWidget(Identifier.of("owo", "braid_inspector_leaf"), false)
                            ),
                            title
                        )
            );
        }
    }
}
