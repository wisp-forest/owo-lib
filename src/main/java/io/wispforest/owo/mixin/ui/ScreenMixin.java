package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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

}
