package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

public class Slider extends RawSlider {

    public Slider(
        double value,
        @Nullable SliderSetupCallback<Slider> setupCallback,
        @Nullable SliderCallback onChanged
    ) {
        super(
            value,
            null,
            onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            8,
            new DefaultSliderHandle()
        );
        if (setupCallback != null) setupCallback.setup(this);
    }

    public Slider(
        double value,
        @Nullable SliderSetupCallback<Slider> setupCallback,
        SliderCallback onChanged,
        boolean active
    ) {
        this(value, setupCallback, active ? onChanged : null);
    }

    //region Setup Methods

    public Slider handleSize(double handleSize) {
        this.assertMutable();
        this.handleSize = handleSize;
        return this;
    }

    public double handleSize() {
        return this.handleSize;
    }

    //TODO: do we let the handle/track be changed?

    //endregion
}
