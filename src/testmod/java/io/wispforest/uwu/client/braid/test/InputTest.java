package io.wispforest.uwu.client.braid.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.scroll.ScrollController;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

import static io.wispforest.owo.braid.framework.instance.InspectorProperty.rounded;

public class InputTest extends StatefulWidget {
    @Override
    public WidgetState<InputTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<InputTest> {
        private final List<Component> inputs = Util.make(() -> {
            var list = new ArrayList<Component>();
            list.add(Component.literal("Help idk how to make this scroll to the bottom when i add shit (everyone laugh at this user)"));
            return list;
        });
        private final ScrollController controller = new ScrollController(this);

        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                250.0,
                null,
                new Panel(
                    Panel.VANILLA_LIGHT,
                    new Padding(
                        Insets.all(8),
                        new Column(
                            Label.literal("Interact with V this V"),
                            new Sized(
                                null, 200,
                                new MouseArea(
                                    area -> area.cursorStyle(CursorStyle.HAND)
                                        .clickCallback((x, y, button, modifiers) -> this.addToList(getMouseButtonName(button)
                                            .append(" pressed at:\n")
                                            .append(formatCoordinates(x, y))))
                                        .releaseCallback((x, y, button, modifiers) -> this.addToList(getMouseButtonName(button)
                                            .append(" released at:\n")
                                            .append(formatCoordinates(x, y))))
                                        .dragStartCallback((button, modifiers) -> this.addToList(getMouseButtonName(button).append(" drag started")))
                                        .dragEndCallback(() -> this.addToList(Component.literal("Drag ended")))
                                        .enterCallback(() -> this.addToList(Component.literal("Mouse entered")))
                                        .exitCallback(() -> this.addToList(Component.literal("Mouse exited"))),
                                    new Focusable(
                                        input ->
                                            input.keyDownCallback((key, modifiers) -> this.addToList(getKeyName(key).append(" pressed")))
                                                .keyUpCallback((key, modifiers) -> this.addToList(getKeyName(key).append(" released")))
                                                .focusGainedCallback(() -> this.addToList(Component.literal("Focus gained")))
                                                .focusLostCallback(() -> this.addToList(Component.literal("Focus lost")))
                                                .charCallback((charCode, modifiers) -> this.addToList(Component.literal("Character typed: \"" + (char) charCode + "\""))),
                                        new Panel(
                                            Panel.VANILLA_INSET,
                                            new VerticallyScrollable(
                                                controller,
                                                null,
                                                new Column(
                                                    new Padding(Insets.vertical(2)),
                                                    this.inputs.stream().map(Label::new).toList()
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            );
        }

        private boolean addToList(Component text) {
            this.setState(() -> {
                this.inputs.add(text);
                this.schedulePostLayoutCallback(() -> {
                    this.controller.jumpTo(this.controller.maxOffset());
                });
            });
            return false; // return false to allow other shit to happen:tm:
        }

        private MutableComponent getKeyName(int key) {
            return Component.empty().append(InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName());
        }

        private MutableComponent getMouseButtonName(int button) {
            return Component.empty().append(InputConstants.Type.MOUSE.getOrCreate(button).getDisplayName());
        }

        private Component formatCoordinates(double x, double y) {
            return Component.literal("[x: " + rounded(x) + ", y: " + rounded(y) + "]");
        }
    }
}
