package io.wispforest.owo.mixin.ui.display;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.events.MouseButtonReleaseEvent;
import io.wispforest.owo.braid.core.events.MouseMoveEvent;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V"))
    public void beforeWorldRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        BraidDisplayBinding.updateAndDrawDisplays();
    }

    @Inject(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"))
    public void updateTargetDisplay(
        float tickDelta,
        CallbackInfo ci,
        @Local Entity camera,
        @Local(ordinal = 0) double reach,
        @Share("camera") LocalRef<Entity> cameraRef,
        @Share("target_display") LocalRef<BraidDisplayBinding.DisplayHitResult> targetDisplay
    ) {
        cameraRef.set(camera);
        targetDisplay.set(
            BraidDisplayBinding.queryTargetDisplay(camera.getCameraPosVec(tickDelta), camera.getRotationVec(tickDelta))
        );
    }

    @Inject(method = "updateCrosshairTarget", at = @At(value = "TAIL"))
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

        var cameraPos = cameraRef.get().getCameraPosVec(tickDelta);
        if (this.client.crosshairTarget.getPos().squaredDistanceTo(cameraPos) > displayHitPoint.squaredDistanceTo(cameraPos)) {
            this.setTargetDisplay(targetDisplay.get());
            BraidDisplayBinding.onDisplayHit(BraidDisplayBinding.targetDisplay);

            var display = BraidDisplayBinding.targetDisplay.display();

            if (display.primaryPressed && !MinecraftClient.getInstance().options.useKey.isPressed()) {
                display.app.eventBinding.add(new MouseButtonReleaseEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, KeyModifiers.NONE));
                display.primaryPressed = false;
            }

            if (display.secondaryPressed && !MinecraftClient.getInstance().options.attackKey.isPressed()) {
                display.app.eventBinding.add(new MouseButtonReleaseEvent(GLFW.GLFW_MOUSE_BUTTON_RIGHT, KeyModifiers.NONE));
                display.secondaryPressed = false;
            }

            this.client.crosshairTarget = BlockHitResult.createMissed(
                this.client.crosshairTarget.getPos(),
                Direction.UP,
                BlockPos.ofFloored(this.client.crosshairTarget.getPos())
            );

            this.client.targetedEntity = null;
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
