package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.ui.layers.Layer.Instance;
import io.wispforest.owo.ui.layers.Layers;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Shadow private int activeButton;

    @Inject(method = "method_55795", at = @At("HEAD"), cancellable = true)
    private void captureScreenMouseDrag(Screen screen, double mouseX, double mouseY, double deltaX, double deltaY, CallbackInfo ci) {
        boolean handled = false;
        for (var instance : Layers.getInstances(screen)) {
            handled = instance.adapter.mouseDragged(mouseX, mouseY, this.activeButton, deltaX, deltaY);
            if (handled) break;
        }

        if (handled) ci.cancel();
    }

}
