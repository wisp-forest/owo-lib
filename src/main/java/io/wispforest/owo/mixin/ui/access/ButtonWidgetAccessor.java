package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Button.class)
public interface ButtonWidgetAccessor {

    @Mutable
    @Accessor("onPress")
    void owo$setOnPress(Button.OnPress onPress);

}
