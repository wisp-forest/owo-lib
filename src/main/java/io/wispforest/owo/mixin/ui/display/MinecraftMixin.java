package io.wispforest.owo.mixin.ui.display;

import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.events.MouseButtonPressEvent;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isHandsBusy()Z"), cancellable = true)
    public void dispatchSecondaryPressEvent(CallbackInfo ci) {
        if (BraidDisplayBinding.targetDisplay == null || BraidDisplayBinding.targetDisplay.display().primaryPressed) return;

        var eventBinding = BraidDisplayBinding.targetDisplay.display().app.eventBinding;
        eventBinding.add(new MouseButtonPressEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, KeyModifiers.NONE));

        BraidDisplayBinding.targetDisplay.display().primaryPressed = true;
        this.player.swing(InteractionHand.MAIN_HAND);

        ci.cancel();
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    public void dispatchPrimaryPressEvent(CallbackInfoReturnable<Boolean> cir) {
        if (BraidDisplayBinding.targetDisplay == null || BraidDisplayBinding.targetDisplay.display().secondaryPressed) return;

        var eventBinding = BraidDisplayBinding.targetDisplay.display().app.eventBinding;
        eventBinding.add(new MouseButtonPressEvent(GLFW.GLFW_MOUSE_BUTTON_RIGHT, KeyModifiers.NONE));

        BraidDisplayBinding.targetDisplay.display().secondaryPressed = true;
        this.player.swing(InteractionHand.MAIN_HAND);

        cir.setReturnValue(true);
    }

}
