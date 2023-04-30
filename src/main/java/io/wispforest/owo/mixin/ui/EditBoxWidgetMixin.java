package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EditBoxWidget.class)
public abstract class EditBoxWidgetMixin extends ScrollableWidget {

    public EditBoxWidgetMixin(int i, int j, int k, int l, Text text) {
        super(i, j, k, l, text);
    }

    @Override
    public void onFocusGained(Component.FocusSource source) {
        super.onFocusGained(source);
        this.setFocused(true);
    }

}
