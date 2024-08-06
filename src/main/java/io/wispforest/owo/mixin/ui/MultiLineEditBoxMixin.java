package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MultiLineEditBox.class)
public abstract class MultiLineEditBoxMixin extends AbstractScrollWidget implements GreedyInputComponent {

    public MultiLineEditBoxMixin(int i, int j, int k, int l, Text text) {
        super(i, j, k, l, text);
    }

    @Override
    public void onFocusGained(Component.FocusSource source) {
        super.onFocusGained(source);
        this.setFocused(true);
    }

}
