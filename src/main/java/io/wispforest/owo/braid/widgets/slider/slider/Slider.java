package io.wispforest.owo.braid.widgets.slider.slider;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.slider.DefaultSliderHandle;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class Slider extends StatelessWidget {

    public final double value;
    public final WidgetSetupCallback<RawSlider> setupCallback;

    public Slider(
        double value,
        WidgetSetupCallback<RawSlider> setupCallback
    ) {
        this.value = value;
        this.setupCallback = setupCallback;
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawSlider(
            this.value,
            new DefaultSliderHandle(),
            widget -> {
                widget
                    .track(new Panel(ButtonComponent.DISABLED_TEXTURE))
                    .handleSize(8);
                setupCallback.setup(widget);
            }
        );
    }
}
