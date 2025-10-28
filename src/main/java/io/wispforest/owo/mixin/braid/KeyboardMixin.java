package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.braid.core.events.CharInputEvent;
import io.wispforest.owo.braid.util.layers.BraidLayersBinding;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @WrapOperation(method = "method_1473", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Element;charTyped(CI)Z"))
    private static boolean captureScreenCharTyped(Element instance, char chr, int modifiers, Operation<Boolean> original) {
        return BraidLayersBinding.tryHandleEvent((Screen) instance, new CharInputEvent(chr, modifiers))
            || original.call(instance, chr, modifiers);
    }

    @WrapOperation(method = "method_1458", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Element;charTyped(CI)Z"))
    private static boolean captureScreenCharTyped2(Element instance, char chr, int modifiers, Operation<Boolean> original) {
        return BraidLayersBinding.tryHandleEvent((Screen) instance, new CharInputEvent(chr, modifiers))
            || original.call(instance, chr, modifiers);
    }
}