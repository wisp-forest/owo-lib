package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.EmptyWidget;
import io.wispforest.owo.braid.widgets.button.ButtonPanel;

public class DefaultSliderHandle extends StatelessWidget {

    public final boolean active;

    public DefaultSliderHandle(boolean active) {
        this.active = active;
    }

    @Override
    public Widget build(BuildContext context) {
        return new ButtonPanel(
            this.active,
            EmptyWidget.INSTANCE
        );
    }
}
