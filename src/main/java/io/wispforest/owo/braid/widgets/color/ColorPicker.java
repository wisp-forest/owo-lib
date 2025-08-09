package io.wispforest.owo.braid.widgets.color;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.slider.slider.Slider;
import io.wispforest.owo.braid.widgets.slider.xlyder.Xlyder;

public class ColorPicker extends StatefulWidget {
    public final ColorController controller;

    public ColorPicker(ColorController controller) {
        this.controller = controller;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<ColorPicker> {

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            var controller = widget.controller;
            return new Column(
                new Sized(
                    50, 50,
                    new Xlyder(
                        controller.hue, controller.saturation,
                        (h ,s) -> this.setState(() -> controller.set((float) h, (float) s, null, null))
                    )
                ),
                new Padding(Size.of(0, 5)),
                new Sized(
                    50, 20,
                    new Slider(
                        controller.value,
                        v -> this.setState(() -> controller.set(null, null, (float) v, null)),
                        LayoutAxis.HORIZONTAL
                    )
                )
            );
        }
    }
}
