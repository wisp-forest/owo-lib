package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;

public class InspectorWidget extends StatefulWidget {

    private final WidgetProxy rootProxy;
    private final WidgetInstance<?> rootInstance;
    private final BraidInspector inspector;

    public InspectorWidget(WidgetProxy rootProxy, WidgetInstance<?> rootInstance, BraidInspector inspector) {
        this.rootProxy = rootProxy;
        this.rootInstance = rootInstance;
        this.inspector = inspector;
    }

    @Override
    public WidgetState<InspectorWidget> createState() {
        return new State();
    }

    public static class State extends StreamListenerState<InspectorWidget> {

        public InspectorState inspectorState;

        @Override
        public void init() {
            this.streamListen(
                widget -> widget.inspector.onRefresh(),
                unit -> setState(() -> {})
            );

            this.streamListen(
                widget -> widget.inspector.onReveal(),
                event -> this.inspectorState.setState(() -> {
                    this.inspectorState.selectedElement = event.instance;
                    this.inspectorState.lastRevealEvent = event;
                })
            );
        }

        @Override
        public Widget build(BuildContext context) {
            return new SharedState<>(
                InspectorState::new,
                new Builder(stateContext -> {
                    this.inspectorState = SharedState.getWithoutDependency(stateContext, InspectorState.class);

                    return new Box(
                        Color.ofRgb(0x1d2026),
                        new Stack(
                            new Scrollable(
                                true,
                                true,
                                null,
                                null,
                                new InstanceTreeView(this.widget().inspector.onReveal(), this.widget().rootInstance)
                            ),
                            new Align(
                                Alignment.BOTTOM_RIGHT,
                                new Padding(
                                    Insets.all(10),
                                    new MessageButton(
                                        Text.literal("🔱"),
                                        () -> this.widget().inspector.pick()
                                    )
                                )
                            )
                        )
                    );
                })
            );
        }
    }
}
