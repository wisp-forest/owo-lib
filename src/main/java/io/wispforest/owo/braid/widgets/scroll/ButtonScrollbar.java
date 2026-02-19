package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.ui.component.ButtonComponent;

public class ButtonScrollbar extends Scrollbar {

    public ButtonScrollbar(LayoutAxis axis, ScrollController controller) {
        super(
            axis,
            controller,
            new Panel(ButtonComponent.DISABLED_TEXTURE),
            new Panel(ButtonComponent.ACTIVE_TEXTURE)
        );
    }

}
