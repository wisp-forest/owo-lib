package io.wispforest.owo.mixin.ui.layers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.ui.layers.Layers;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @WrapOperation(method = "handleAccumulatedMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"))
    private boolean captureScreenMouseDrag(Screen screen, MouseButtonEvent click, double deltaX, double deltaY, Operation<Boolean> original) {
        boolean handled = false;
        for (var instance : Layers.getInstances(screen)) {
            handled = instance.adapter.mouseDragged(click, deltaX, deltaY);
            if (handled) break;
        }

        return handled || original.call(screen, click, deltaX, deltaY);
    }
}
