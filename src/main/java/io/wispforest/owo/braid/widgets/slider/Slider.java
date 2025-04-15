package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.Key;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.ui.component.ButtonComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class Slider extends StatelessWidget {

    public final double value;
    public final double min;
    public final double max;
    public final @Nullable Double step;

    public final DoubleConsumer onChanged;

    public Slider(double value, double min, double max, @Nullable Double step, DoubleConsumer onChanged) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.onChanged = onChanged;
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawSlider(
            this.value,
            this.min,
            this.max,
            this.step,
            this.onChanged,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            new DefaultHandle(),
            8
        );
    }
}

class DefaultHandle extends StatefulWidget {
    @Override
    public WidgetState<DefaultHandle> createState() {
        return new DefaultHandleState();
    }
}

class DefaultHandleState extends WidgetState<DefaultHandle> {

    private boolean hovered = false;

    @Override
    public Widget build(BuildContext context) {
        return new MouseArea(
            widget -> widget
                .enterCallback(() -> setState(() -> this.hovered = true))
                .exitCallback(() -> setState(() -> this.hovered = false)),
            new Panel(this.hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE).key(Key.of("slider-handle"))
        );
    }
}
