package com.glisco.owo.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerInvoker {

    @Invoker("insertItem")
    boolean owo$insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast);

}
