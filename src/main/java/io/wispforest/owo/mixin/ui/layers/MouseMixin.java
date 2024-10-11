package io.wispforest.owo.mixin.ui.layers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.ui.layers.Layers;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow private int activeButton;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseDragged(DDIDD)Z"))
    private boolean captureScreenMouseDrag(Screen screen, double mouseX, double mouseY, int i, double deltaX, double deltaY, Operation<Boolean> original) {
        boolean handled = false;
        for (var instance : Layers.getInstances(screen)) {
            handled = instance.adapter.mouseDragged(mouseX, mouseY, this.activeButton, deltaX, deltaY);
            if (handled) break;
        }

        return handled || original.call(screen, mouseX, mouseY, i, deltaX, deltaY);
    }

}
