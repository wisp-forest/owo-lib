package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Hoverable;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.ui.component.ButtonComponent;

public class ButtonPanel extends StatelessWidget {
    public final boolean active;
    public final Widget child;

    public ButtonPanel(boolean active, Widget child) {
        this.active = active;
        this.child = child;
    }

    @Override
    public Widget build(BuildContext context) {
        return new Hoverable(
            hovered ->
                new Panel(
                    this.active
                        ? hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE
                        : ButtonComponent.DISABLED_TEXTURE,
                    new Padding(
                        Insets.all(5),
                        this.child
                    )
                )
        );
    }
}
