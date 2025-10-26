package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.braid.core.events.CharInputEvent;
import io.wispforest.owo.braid.util.layers.BraidLayersBinding;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @WrapOperation(method = "onChar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;charTyped(Lnet/minecraft/client/input/CharInput;)Z"))
    private boolean captureScreenCharTyped(Screen screen, CharInput charInput, Operation<Boolean> original) {
        return BraidLayersBinding.tryHandleEvent(screen, new CharInputEvent((char) charInput.codepoint(), charInput.modifiers()))
            || original.call(screen, charInput);
    }
}