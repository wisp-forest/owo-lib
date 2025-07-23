package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.KeyboardInput;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;

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

            var instanceName = this.widget().instance.getClass().getTypeName();
            var matcher = Pattern.compile("^.*?([A-Za-z]\\w+\\$Instance)$").matcher(instanceName);

            if (matcher.matches()) {
                instanceName = matcher.group(1).replaceAll("\\$", ".");
            }

            var title = new Box(
                selected ? new Color(1f, 1f, 1f, .25f) : Color.ofArgb(0),
                new Row(
                    new Label(Text.literal(instanceName).styled(style -> style.withBold(this.hovered)))
                    // TODO: relayout boundary icon
                )
            );

            return new KeyboardInput(
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

        private static final Pattern INSTANCE_NAME_PATTERN = Pattern.compile("^.*?([A-Za-z]\\w+\\$Instance)$");
    }
}
