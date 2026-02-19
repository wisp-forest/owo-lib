package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.inject.GreedyInputUIComponent;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MultiLineEditBox.class)
public abstract class MultiLineEditBoxMixin extends AbstractScrollArea implements GreedyInputUIComponent {

    public MultiLineEditBoxMixin(int i, int j, int k, int l, Component text) {
        super(i, j, k, l, text);
    }

    @Override
    public void onFocusGained(UIComponent.FocusSource source) {
        super.onFocusGained(source);
        this.setFocused(true);
    }

}
