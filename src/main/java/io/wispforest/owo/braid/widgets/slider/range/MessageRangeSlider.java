package io.wispforest.owo.braid.widgets.slider.range;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class MessageRangeSlider extends StatelessWidget {

    public final double minValue, maxValue;
    public final @Nullable WidgetSetupCallback<RangeSlider> setupCallback;
    public final @Nullable RangeSliderCallback onChanged;
    public final Text message;

    public MessageRangeSlider(
        double minValue,
        double maxValue,
        Text message,
        @Nullable WidgetSetupCallback<RangeSlider> setupCallback,
        @Nullable RangeSliderCallback onChanged
    ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.setupCallback = setupCallback;
        this.onChanged = onChanged;
        this.message = message;
    }

    public MessageRangeSlider(
        double minValue,
        double maxValue,
        Text message,
        @Nullable WidgetSetupCallback<RangeSlider> setupCallback,
        boolean active,
        RangeSliderCallback onChanged
    ) {
        this(minValue, maxValue, message, setupCallback, active ? onChanged : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Stack(
            new RangeSlider(
                this.minValue,
                this.maxValue,
                this.setupCallback,
                this.onChanged
            ),
            new Label(
                LabelStyle.SHADOW,
                false,
                this.message
            )
        );
    }
}
