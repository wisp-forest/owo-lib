package io.wispforest.owo.mixin.ui.layers;

import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    MinecraftClient client;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/Screen;method_47413(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"
            )
    )
    private void renderLayers(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        for (var layer : ((OwoScreenExtension) this.client.currentScreen).owo$getLayers()) {
            if (layer.aggressivePositioning) layer.dispatchLayoutUpdates();
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/Screen;method_47413(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    @SuppressWarnings("InvalidInjectorMethodSignature")
    private void renderLayers(float tickDelta, long startTime, boolean tick, CallbackInfo ci, int mouseX, int mouseY, MatrixStack matrixStack) {
        for (var layer : ((OwoScreenExtension) this.client.currentScreen).owo$getLayers()) {
            layer.adapter.render(matrixStack, mouseX, mouseY, this.client.getLastFrameDuration());
        }
    }

}
