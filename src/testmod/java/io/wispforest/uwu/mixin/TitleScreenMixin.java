package io.wispforest.uwu.mixin;

import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.uwu.Uwu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "method_41198", at = @At("HEAD"), cancellable = true)
    private void injectUwuConfigScreen(Button button, CallbackInfo ci) {
        var alt = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_ALT)
            || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
        if (!alt) return;

        Minecraft.getInstance().setScreen(ConfigScreen.create(Uwu.BRUHHHHH, this));
        ci.cancel();
    }

}
