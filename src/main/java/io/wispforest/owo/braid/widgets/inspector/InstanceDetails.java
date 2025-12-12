package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.checkbox.Checkbox;
import io.wispforest.owo.braid.widgets.checkbox.CheckboxStyle;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class InstanceDetails extends StatefulWidget {

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<InstanceDetails> {
        @Override
        public Widget build(BuildContext context) {
            var selected = SharedState.select(context, InspectorState.class, state -> state.selectedElement);

            List<Widget> children;
            if (selected instanceof WidgetInstance<?> instance) {
                var instanceClassName = instance.getClass().getName();
                var matcher = INSTANCE_NAME_PATTERN.matcher(instanceClassName);
                instanceClassName = matcher.matches() ? matcher.group(1) : instanceClassName;

                children = new ArrayList<>();
                children.add(new Grid(
                    LayoutAxis.VERTICAL,
                    2,
                    Grid.CellFit.tight(),
                    colorRows(
                        Color.rgb(0x111319),
                        2,
                        gatherProperties(instance).stream().<Widget>map(Label::new).toList()
                    )
                ));

                if (instance.debugHasVisualizers()) {
                    children.add(new Padding(
                        Insets.of(5, 0, 5, 0),
                        new Row(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Checkbox(
                                CheckboxStyle.BRAID,
                                instance.debugDrawVisualizers,
                                nowChecked -> setState(() -> {
                                    instance.debugDrawVisualizers = nowChecked;
                                })
                            ),
                            new Padding(
                                Insets.left(5),
                                Label.literal("draw visualizers")
                            )
                        )
                    ));
                }

                children.addAll(List.of(
                    new Flexible(new Padding(Insets.none())),
                    new Label(Component.literal(instanceClassName))
                ));
            } else {
                children = List.of(new Flexible(
                    new Center(
                        new Label(Component.literal("no instance selected"))
                    )
                ));
            }

            return new Row(
                new Sized(1, null, new Box(Color.WHITE)),
                new Sized(
                    150,
                    null,
                    new Column(
                        Stream.concat(
                            Stream.of(new Padding(Insets.bottom(3), new Label(Component.literal("Instance Details")))),
                            children.stream()
                        ).toList()
                    )
                )
            );
        }

        private static List<Component> gatherProperties(WidgetInstance<?> instance) {
            var instanceTransform = instance.hasParent() ? instance.parent().computeGlobalTransform().invert() : new Matrix3x2f();
            var absPos = instanceTransform.transformPosition((float) instance.transform.x(), (float) instance.transform.y(), new Vector2f());

            var properties = new ArrayList<>(List.<Component>of(
                    Component.literal("Rel. Position").withStyle(ChatFormatting.BOLD),
                    Component.literal(rounded(instance.transform.x()) + ", " + rounded(instance.transform.y())),
                    Component.literal("Abs. Position").withStyle(ChatFormatting.BOLD),
                    Component.literal(rounded(absPos.x()) + ", " + rounded(absPos.y())),
                    Component.literal("Width").withStyle(ChatFormatting.BOLD),
                    Component.literal(instance.transform.width() + "px"),
                    Component.literal("Height").withStyle(ChatFormatting.BOLD),
                    Component.literal(instance.transform.height() + "px"),
                    Component.literal("Widget").withStyle(ChatFormatting.BOLD),
                    Component.literal(instance.widget().getClass().getSimpleName())
            ));

            for (var property : instance.debugListInspectorProperties()) {
                properties.add(property.name().copy().withStyle(ChatFormatting.BOLD));
                properties.add(property.value());
            }

            return properties;
        }

        private static List<Widget> colorRows(Color alternateColor, int crossAxisCells, List<Widget> cells) {
            var result = new ArrayList<Widget>();

            var mainAxisIdx = 0;
            var crossAxisIdx = 0;
            for (var widget : cells) {
                widget = new Padding(Insets.vertical(2), widget);

                if (mainAxisIdx % 2 == 0) {
                    result.add(widget);
                } else {
                    result.add(new Box(alternateColor, false, widget));
                }

                if (++crossAxisIdx == crossAxisCells) {
                    crossAxisIdx = 0;
                    mainAxisIdx++;
                }
            }

            return result;
        }

        private static String rounded(double value) {
            return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
        }

        // ---

        private static final Pattern INSTANCE_NAME_PATTERN = Pattern.compile("^.*?([A-Za-z]\\w+\\$?Instance)$");
    }
}
