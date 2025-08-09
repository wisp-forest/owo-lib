package io.wispforest.owo.braid.widgets.slider.drag;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class MessageDrag extends StatelessWidget {

    public final double value;
    public final WidgetSetupCallback<RawDrag> setupCallback;

    public final Text message;

    public MessageDrag(
        double value,
        WidgetSetupCallback<RawDrag> setupCallback,
        Text message
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
        this.message = message;
    }

    @Override
    public Widget build(BuildContext context) {
        return new Drag(
            this.value,
            this.setupCallback,
            new Label(
                LabelStyle.SHADOW,
                false,
                this.message
            )
        );
    }
}
