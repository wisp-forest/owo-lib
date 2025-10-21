package io.wispforest.owo.mixin.ui.display;

import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.braid.core.events.MouseScrollEvent;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"), cancellable = true)
    public void scrollBraidDisplays(long window, double horizontal, double vertical, CallbackInfo ci, @Local(ordinal = 3) double xOffset, @Local(ordinal = 4) double yOffset) {
        if (BraidDisplayBinding.targetDisplay == null || this.client.player.isSneaking()) return;

        BraidDisplayBinding.targetDisplay.display().app.eventBinding.add(new MouseScrollEvent(xOffset, yOffset));
        ci.cancel();
    }

}
