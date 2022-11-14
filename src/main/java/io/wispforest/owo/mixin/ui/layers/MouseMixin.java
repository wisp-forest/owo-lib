package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow private int activeButton;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "method_1611", at = @At("HEAD"), cancellable = true)
    private static void captureScreenMouseClick(boolean[] bls, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
        boolean handled = false;
        for (var layer : ((OwoScreenExtension) screen).owo$getLayers()) {
            handled = layer.adapter.mouseClicked(mouseX, mouseY, button);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Inject(method = "method_1605", at = @At("HEAD"), cancellable = true)
    private static void captureScreenMouseRelease(boolean[] bls, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
        boolean handled = false;
        for (var layer : ((OwoScreenExtension) screen).owo$getLayers()) {
            handled = layer.adapter.mouseReleased(mouseX, mouseY, button);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Inject(method = "method_1602", at = @At("HEAD"), cancellable = true)
    private void captureScreenMouseDrag(Screen screen, double mouseX, double mouseY, double deltaX, double deltaY, CallbackInfo ci) {
        boolean handled = false;
        for (var layer : ((OwoScreenExtension) screen).owo$getLayers()) {
            handled = layer.adapter.mouseDragged(mouseX, mouseY, this.activeButton, deltaX, deltaY);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDD)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void captureScreenMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci, double amount, double mouseX, double mouseY) {
        boolean handled = false;
        for (var layer : ((OwoScreenExtension) this.client.currentScreen).owo$getLayers()) {
            handled = layer.adapter.mouseScrolled(mouseX, mouseY, amount);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

}
