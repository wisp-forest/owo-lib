package io.wispforest.owo.braid.widgets.slider.drag;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

public class Drag extends StatelessWidget {

    public final double value;
    public final WidgetSetupCallback<RawDrag> setupCallback;

    public final @Nullable Widget child;

    public Drag(
        double value,
        WidgetSetupCallback<RawDrag> setupCallback,
        @Nullable Widget child
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
        this.child = child;
    }

    @Override
    public Widget build(BuildContext context) {
        var panel = new Panel(ButtonComponent.DISABLED_TEXTURE);
        return new RawDrag(
            this.value,
            this.setupCallback,
            this.child == null
                ? panel
                : new Stack(panel, this.child)
        );
    }
}
