package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.DefaultLabelStyle;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.FlatScrollbar;
import io.wispforest.owo.braid.widgets.scroll.ScrollableWithBars;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

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
        public boolean alwaysOnTop = true;

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
                        new DefaultLabelStyle(
                            new LabelStyle(null, null, Style.EMPTY.withFont(MinecraftClient.UNICODE_FONT_ID), null),
                            new Padding(
                                Insets.all(2),
                                new Row(
                                    new Flexible(
                                        new Stack(
                                            new ScrollableWithBars(
                                                null,
                                                null,
                                                3,
                                                (axis, controller) -> new FlatScrollbar(axis, controller, Color.ofRgb(0xabb0bf), Color.ofRgb(0xabb0bf)),
                                                new InstanceTreeView(this.widget().inspector.onReveal(), this.widget().rootInstance)
                                            ),
                                            new Align(
                                                Alignment.BOTTOM_RIGHT,
                                                new Padding(
                                                    Insets.all(5),
                                                    new Row(
                                                        new Padding(Insets.horizontal(1)),
                                                        List.of(
                                                            new Sized(
                                                                20,
                                                                20,
                                                                new Tooltip(
                                                                    Text.literal(this.alwaysOnTop ? "window behavior:\nalways on top" : "window behavior:\nnormal"),
                                                                    new Button(
                                                                        () -> this.setState(() -> {
                                                                            this.alwaysOnTop = !this.alwaysOnTop;
                                                                            GLFW.glfwSetWindowAttrib(this.widget().inspector.currentWindow.handle, GLFW.GLFW_FLOATING, this.alwaysOnTop ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
                                                                        }),
                                                                        new SpriteWidget(
                                                                            this.alwaysOnTop
                                                                                ? Identifier.of("owo", "braid_inspector_always_on_top")
                                                                                : Identifier.of("owo", "braid_inspector_not_always_on_top"),
                                                                            false
                                                                        )
                                                                    )
                                                                )
                                                            ),
                                                            new Sized(
                                                                20,
                                                                20,
                                                                new Button(
                                                                    () -> this.widget().inspector.pick(),
                                                                    new SpriteWidget(Identifier.of("owo", "braid_inspector_pick"), false)
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    ),
                                    new InstanceDetails()
                                )
                            )
                        )
                    );
                })
            );
        }
    }
}
