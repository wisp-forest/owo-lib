package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.ui.component.ButtonComponent;

public class ButtonPanel extends StatefulWidget {
    public final boolean active;
    public final Widget child;

    public ButtonPanel(boolean active, Widget child) {
        this.active = active;
        this.child = child;
    }

    @Override
    public WidgetState<ButtonPanel> createState() {
        return new State();
    }

    public static class State extends WidgetState<ButtonPanel> {
        private boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false)),
                new Panel(
                    this.widget().active
                        ? this.hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE
                        : ButtonComponent.DISABLED_TEXTURE,
                    new Padding(
                        Insets.all(5),
                        this.widget().child
                    )
                )
            );
        }
    }
}
