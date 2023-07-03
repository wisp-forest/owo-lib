package io.wispforest.owo.mixin.ui.layers;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

    @Accessor("x")
    int owo$getRootX();

    @Accessor("y")
    int owo$getRootY();

}
