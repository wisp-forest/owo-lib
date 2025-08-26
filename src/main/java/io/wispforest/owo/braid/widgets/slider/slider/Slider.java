package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

public class Slider extends StatelessWidget {

    public final double value;
    public final @Nullable WidgetSetupCallback<RawSlider> setupCallback;
    public final @Nullable SliderCallback onChanged;

    public Slider(
        double value,
        @Nullable WidgetSetupCallback<RawSlider> setupCallback,
        @Nullable SliderCallback onChanged
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
        this.onChanged = onChanged;
    }

    public Slider(
        double value,
        @Nullable WidgetSetupCallback<RawSlider> setupCallback,
        SliderCallback onChanged,
        boolean active
    ) {
        this(value, setupCallback, active ? onChanged : null);
    }


    @Override
    public Widget build(BuildContext context) {
        return new RawSlider(
            this.value,
            this.setupCallback,
            this.onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            new DefaultSliderHandle()
        );
    }
}
