package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.Key;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.ui.component.ButtonComponent;

public class DefaultSliderHandle extends StatefulWidget {
    @Override
    public WidgetState<DefaultSliderHandle> createState() {
        return new State();
    }

    public static class State extends WidgetState<DefaultSliderHandle> {

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
}
