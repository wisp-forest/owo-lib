package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "method_1473", at = @At("HEAD"), cancellable = true)
    private static void captureScreenCharTyped(Element element, char character, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        for (var layer : ((OwoScreenExtension) MinecraftClient.getInstance().currentScreen).owo$getLayers()) {
            handled = layer.adapter.charTyped(character, modifiers);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Inject(method = "method_1458", at = @At("HEAD"), cancellable = true)
    private static void captureScreenCharTyped(Element element, int character, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        for (var layer : ((OwoScreenExtension) MinecraftClient.getInstance().currentScreen).owo$getLayers()) {
            handled = layer.adapter.charTyped((char) character, modifiers);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Inject(method = "method_1454", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;applyKeyPressNarratorDelay()V"), cancellable = true)
    private void captureScreenKeyPressed(int action, Screen screen, boolean[] bls, int keycode, int scancode, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        for (var layer : ((OwoScreenExtension) MinecraftClient.getInstance().currentScreen).owo$getLayers()) {
            handled = layer.adapter.keyPressed(keycode, scancode, modifiers);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Inject(method = "method_1454", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyReleased(III)Z"), cancellable = true)
    private void captureScreenKeyReleased(int action, Screen screen, boolean[] bls, int keycode, int scancode, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        for (var layer : ((OwoScreenExtension) MinecraftClient.getInstance().currentScreen).owo$getLayers()) {
            handled = layer.adapter.keyReleased(keycode, scancode, modifiers);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

}
