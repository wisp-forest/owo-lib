package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.regex.Pattern;

public class InstanceTitle extends StatefulWidget {
    public final WidgetInstance<?> instance;
    public InstanceTitle(WidgetInstance<?> instance) {
        this.instance = instance;
    }

    @Override
    public WidgetState<InstanceTitle> createState() {
        return new State();
    }

    public static class State extends WidgetState<InstanceTitle> {

        public boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            var selected = SharedState.select(context, InspectorState.class, state -> state.selectedElement) == this.widget().instance;

            var instanceName = this.widget().instance.getClass().getName();
            var matcher = INSTANCE_NAME_PATTERN.matcher(instanceName);

            if (matcher.matches()) {
                instanceName = matcher.group(1).replaceAll("\\$", ".");
            }

            var title = new Panel(
                selected ? Owo.id("braid_inspector_selected") : null,
                new Padding(
                    Insets.all(2),
                    new Row(
                        MainAxisAlignment.START,
                        CrossAxisAlignment.CENTER,
                        new Label(Component.literal(instanceName).withStyle(style -> style.withBold(this.hovered))),
                        new Visibility(
                            this.widget().instance.isRelayoutBoundary() && this.widget().instance.debugParentHasDependency(),
                            new Padding(
                                Insets.left(2),
                                new Tooltip(
                                    Component.literal("Relayout Boundary\n").append(Component.literal("with parent dependency").withStyle(ChatFormatting.GRAY)),
                                    new SpriteWidget(Owo.id("braid_inspector_relayout_boundary_with_dependency"))
                                )
                            )
                        ),
                        new Visibility(
                            this.widget().instance.isRelayoutBoundary() && !this.widget().instance.debugParentHasDependency(),
                            new Padding(
                                Insets.left(2),
                                new Tooltip(
                                    Component.literal("Relayout Boundary"),
                                    new SpriteWidget(Owo.id("braid_inspector_relayout_boundary"))
                                )
                            )
                        ),
                        new Visibility(
                            (this.widget().instance.flags & WidgetInstance.FLAG_HIT_TEST_BOUNDARY) != 0,
                            new Padding(
                                Insets.left(2),
                                new Tooltip(
                                    Component.literal("Hit Test Boundary"),
                                    new SpriteWidget(Owo.id("braid_inspector_hit_test_boundary"))
                                )
                            )
                        )
                    )
                )
            );

            return new Focusable(
                widget -> widget
                    .focusGainedCallback(() -> SharedState.set(context, InspectorState.class, state -> state.selectedElement = this.widget().instance)),
                new MouseArea(
                    widget -> widget
                        .enterCallback(() -> this.setState(() -> {
                            this.widget().instance.debugHighlighted = true;
                            this.hovered = true;
                        }))
                        .exitCallback(() -> this.setState(() -> {
                            this.widget().instance.debugHighlighted = false;
                            this.hovered = false;
                        }))
                        .cursorStyle(CursorStyle.CROSSHAIR),
                    title
                )
            );
        }

        // ---

        private static final Pattern INSTANCE_NAME_PATTERN = Pattern.compile("^.*?([A-Za-z]\\w+)\\$?Instance$");
    }
}
