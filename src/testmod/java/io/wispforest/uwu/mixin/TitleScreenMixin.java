package io.wispforest.uwu.mixin;

import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.uwu.Uwu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "method_41198", at = @At("HEAD"), cancellable = true)
    private void injectUwuConfigScreen(ButtonWidget button, CallbackInfo ci) {
        if (!Screen.hasAltDown()) return;

        MinecraftClient.getInstance().setScreen(ConfigScreen.create(Uwu.BRUHHHHH, this));
        ci.cancel();
    }

}
