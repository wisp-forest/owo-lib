package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.ui.layers.Layers;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "method_1473", at = @At("HEAD"), cancellable = true)
    private static void captureScreenCharTyped(Element element, char character, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        for (var instance : Layers.getInstances(MinecraftClient.getInstance().currentScreen)) {
            handled = instance.adapter.charTyped(character, modifiers);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

    @Inject(method = "method_1458", at = @At("HEAD"), cancellable = true)
    private static void captureScreenCharTyped(Element element, int character, int modifiers, CallbackInfo ci) {
        boolean handled = false;
        for (var instance : Layers.getInstances(MinecraftClient.getInstance().currentScreen)) {
            handled = instance.adapter.charTyped((char) character, modifiers);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

}
