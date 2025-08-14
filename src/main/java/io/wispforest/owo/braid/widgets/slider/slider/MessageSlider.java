package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class MessageSlider extends StatelessWidget {

    public final double value;
    public final Text message;
    public final WidgetSetupCallback<RawSlider> setupCallback;

    public MessageSlider(
        double value,
        Text message,
        WidgetSetupCallback<RawSlider> setupCallback
    ) {
        this.value = value;
        this.message = message;
        this.setupCallback = setupCallback;
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new Slider(
                this.value,
                setupCallback
            ),
            new Label(
                LabelStyle.SHADOW,
                false,
                this.message
            )
        );
    }
}
