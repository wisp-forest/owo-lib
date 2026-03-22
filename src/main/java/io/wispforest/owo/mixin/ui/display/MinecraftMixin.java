package io.wispforest.owo.mixin.ui.display;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.events.MouseButtonPressEvent;
import io.wispforest.owo.braid.core.events.MouseButtonReleaseEvent;
import io.wispforest.owo.braid.core.events.MouseMoveEvent;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @org.jspecify.annotations.Nullable
    public HitResult hitResult;
    @Shadow
    @org.jspecify.annotations.Nullable
    public Entity crosshairPickEntity;
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

    @Inject(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;"))
    public void updateTargetDisplay(
        float tickDelta,
        CallbackInfo ci,
        @Local Entity camera,
        @Share("target_display") LocalRef<BraidDisplayBinding.DisplayHitResult> targetDisplay
    ) {
        targetDisplay.set(
            BraidDisplayBinding.queryTargetDisplay(camera.getEyePosition(tickDelta), camera.getViewVector(tickDelta))
        );
    }

    @Inject(method = "pick", at = @At(value = "TAIL"))
    public void checkDisplayHitTest(
        float tickDelta,
        CallbackInfo ci,
        @Local Entity camera,
        @Share("target_display") LocalRef<BraidDisplayBinding.DisplayHitResult> targetDisplay
    ) {
        if (targetDisplay.get() == null) {
            this.setTargetDisplay(null);
            return;
        }

        var displayHitPoint = targetDisplay.get().display().quad.unproject(targetDisplay.get().point());

        var cameraPos = camera.getEyePosition(tickDelta);
        if (this.hitResult.getLocation().distanceToSqr(cameraPos) > displayHitPoint.distanceToSqr(cameraPos)) {
            this.setTargetDisplay(targetDisplay.get());
            BraidDisplayBinding.onDisplayHit(BraidDisplayBinding.targetDisplay);

            var display = BraidDisplayBinding.targetDisplay.display();

            if (display.primaryPressed && !Minecraft.getInstance().options.keyUse.isDown()) {
                display.app.eventBinding.add(new MouseButtonReleaseEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, KeyModifiers.NONE));
                display.primaryPressed = false;
            }

            if (display.secondaryPressed && !Minecraft.getInstance().options.keyAttack.isDown()) {
                display.app.eventBinding.add(new MouseButtonReleaseEvent(GLFW.GLFW_MOUSE_BUTTON_RIGHT, KeyModifiers.NONE));
                display.secondaryPressed = false;
            }

            this.hitResult = BlockHitResult.miss(
                this.hitResult.getLocation(),
                Direction.UP,
                BlockPos.containing(this.hitResult.getLocation())
            );

            this.crosshairPickEntity = null;
        } else {
            this.setTargetDisplay(null);
        }
    }

    @Unique
    private void setTargetDisplay(@Nullable BraidDisplayBinding.DisplayHitResult newTarget) {
        if (BraidDisplayBinding.targetDisplay == null) {
            BraidDisplayBinding.targetDisplay = newTarget;
            return;
        }

        if (newTarget == null || BraidDisplayBinding.targetDisplay.display() != newTarget.display()) {
            BraidDisplayBinding.targetDisplay.display().app.eventBinding.add(new MouseMoveEvent(0, 0));
        }

        BraidDisplayBinding.targetDisplay = newTarget;
    }
}
