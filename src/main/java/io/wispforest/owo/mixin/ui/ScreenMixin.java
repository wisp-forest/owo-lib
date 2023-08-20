package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.window.context.CurrentWindowContext;
import io.wispforest.owo.ui.window.context.VanillaWindowContext;
import io.wispforest.owo.ui.window.context.WindowContext;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Screen.class)
public class ScreenMixin {

    @ModifyExpressionValue(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;shouldCloseOnEsc()Z", ordinal = 0))
    private boolean dontCloseOwoScreens(boolean original) {
        //noinspection ConstantValue
        if ((Object) this instanceof BaseOwoScreen<?> || (Object) this instanceof BaseOwoHandledScreen<?, ?>) {
            return false;
        }

        return original;
    }

    @ModifyArg(method = "confirmLink", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), index = 0)
    private @Nullable Screen injectProperLinkSource(@Nullable Screen screen) {
        if ((Object) this != OwoUIDrawContext.utilityScreen()) return screen;
        return OwoUIDrawContext.utilityScreen().getAndClearLinkSource();
    }

    @ModifyArg(method = {"hasShiftDown", "hasControlDown", "hasAltDown"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;isKeyPressed(JI)Z"), index = 0)
    private static long replaceOnOtherWindow(long handle) {
        WindowContext ctx = CurrentWindowContext.current();

        return ctx != VanillaWindowContext.MAIN ? ctx.handle() : handle;
    }
}
