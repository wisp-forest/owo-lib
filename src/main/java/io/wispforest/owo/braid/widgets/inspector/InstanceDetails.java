package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class InstanceDetails extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        var selected = SharedState.select(context, InspectorState.class, state -> state.selectedElement);

        List<Widget> children;
        if (selected instanceof WidgetInstance<?> instance) {
            var instanceTransform = instance.computeGlobalTransform();
            var absPos = new Vector4f((float) instance.transform.x(), (float) instance.transform.y(), 0f, 1f).mul(instanceTransform);

            var instanceClassName = instance.getClass().getName();
            var matcher = INSTANCE_NAME_PATTERN.matcher(instanceClassName);
            instanceClassName = matcher.matches() ? matcher.group(1) : instanceClassName;

            children = List.of(
                new Grid(
                    LayoutAxis.VERTICAL,
                    2,
                    Grid.CellFit.tight(),
                    this.colorRows(
                        Color.ofRgb(0x111319),
                        2,
                        List.of(
                            new Label(Text.literal("Rel. Position").formatted(Formatting.BOLD)),
                            new Label(Text.literal(instance.transform.x() + ", " + instance.transform.y())),
                            new Label(Text.literal("Abs. Position").formatted(Formatting.BOLD)),
                            new Label(Text.literal(absPos.x() + ", " + absPos.y())),
                            new Label(Text.literal("Width").formatted(Formatting.BOLD)),
                            new Label(Text.literal(instance.transform.width() + "px")),
                            new Label(Text.literal("Height").formatted(Formatting.BOLD)),
                            new Label(Text.literal(instance.transform.height() + "px")),
                            new Label(Text.literal("Widget").formatted(Formatting.BOLD)),
                            new Label(Text.literal(instance.widget().getClass().getSimpleName()))
                        )
                    )
                ),
                new Flexible(new Padding(Insets.none())),
                new Label(Text.literal(instanceClassName))
            );
        } else {
            children = List.of(new Flexible(
                new Center(
                    new Label(Text.literal("no instance selected"))
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
                        Stream.of(new Padding(Insets.bottom(3), new Label(Text.literal("Instance Details")))),
                        children.stream()
                    ).toList()
                )
            )
        );
    }

    private List<Widget> colorRows(Color alternateColor, int crossAxisCells, List<Widget> cells) {
        var result = new ArrayList<Widget>();

        var mainAxisIdx = 0;
        var crossAxisIdx = 0;
        for (var widget : cells) {
            widget = new Padding(Insets.vertical(2), widget);

            if (mainAxisIdx % 2 == 0) {
                result.add(widget);
            } else {
                result.add(new Box(alternateColor, widget));
            }

            if (++crossAxisIdx == crossAxisCells) {
                crossAxisIdx = 0;
                mainAxisIdx++;
            }
        }

        return result;
    }

    // ---

    private static final Pattern INSTANCE_NAME_PATTERN = Pattern.compile("^.*?([A-Za-z]\\w+\\$?Instance)$");
}
