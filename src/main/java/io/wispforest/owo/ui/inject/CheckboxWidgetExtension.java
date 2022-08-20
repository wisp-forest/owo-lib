package io.wispforest.owo.ui.inject;

import net.minecraft.client.gui.widget.CheckboxWidget;

public interface CheckboxWidgetExtension {

    default CheckboxWidget checked(boolean checked) {
        throw new IllegalStateException("Interface default method called");
    }

}
