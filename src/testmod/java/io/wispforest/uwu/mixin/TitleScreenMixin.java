package io.wispforest.uwu.mixin;

import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.uwu.Uwu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "lambda$init$4", at = @At("HEAD"), cancellable = true)
    private void injectUwuConfigScreen(Button button, CallbackInfo ci) {
        var alt = Minecraft.getInstance().hasAltDown();
        if (!alt) return;

        Minecraft.getInstance().setScreenAndShow(ConfigScreen.create(Uwu.BRUHHHHH, this));
        ci.cancel();
    }

}
