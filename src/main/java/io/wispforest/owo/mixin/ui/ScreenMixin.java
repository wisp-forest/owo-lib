package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.window.context.CurrentWindowContext;
import io.wispforest.owo.ui.window.context.VanillaWindowContext;
import io.wispforest.owo.ui.window.context.WindowContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Screen.class)
public class ScreenMixin {
    @ModifyArg(method = {"hasShiftDown", "hasControlDown", "hasAltDown"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;isKeyPressed(JI)Z"), index = 0)
    private static long replaceOnOtherWindow(long handle) {
        WindowContext ctx = CurrentWindowContext.current();

        return ctx != VanillaWindowContext.MAIN ? ctx.handle() : handle;
    }
}