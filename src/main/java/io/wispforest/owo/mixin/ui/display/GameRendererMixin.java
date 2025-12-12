package io.wispforest.owo.mixin.ui.display;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.events.MouseButtonReleaseEvent;
import io.wispforest.owo.braid.core.events.MouseMoveEvent;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V"))
    public void beforeWorldRender(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        BraidDisplayBinding.updateAndDrawDisplays();
    }

    @Inject(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;"))
    public void updateTargetDisplay(
        float tickDelta,
        CallbackInfo ci,
        @Local Entity camera,
        @Share("camera") LocalRef<Entity> cameraRef,
        @Share("target_display") LocalRef<BraidDisplayBinding.DisplayHitResult> targetDisplay
    ) {
        cameraRef.set(camera);
        targetDisplay.set(
            BraidDisplayBinding.queryTargetDisplay(camera.getEyePosition(tickDelta), camera.getViewVector(tickDelta))
        );
    }

    @Inject(method = "pick", at = @At(value = "TAIL"))
    public void checkDisplayHitTest(
        float tickDelta,
        CallbackInfo ci,
        @Share("camera") LocalRef<Entity> cameraRef,
        @Share("target_display") LocalRef<BraidDisplayBinding.DisplayHitResult> targetDisplay
    ) {
        if (targetDisplay.get() == null) {
            this.setTargetDisplay(null);
            return;
        }

        var displayHitPoint = targetDisplay.get().display().quad.unproject(targetDisplay.get().point());

        var cameraPos = cameraRef.get().getEyePosition(tickDelta);
        if (this.minecraft.hitResult.getLocation().distanceToSqr(cameraPos) > displayHitPoint.distanceToSqr(cameraPos)) {
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

            this.minecraft.hitResult = BlockHitResult.miss(
                this.minecraft.hitResult.getLocation(),
                Direction.UP,
                BlockPos.containing(this.minecraft.hitResult.getLocation())
            );

            this.minecraft.crosshairPickEntity = null;
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
