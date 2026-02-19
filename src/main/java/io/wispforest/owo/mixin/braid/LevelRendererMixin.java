package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;submitBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeStorage;)V", shift = At.Shift.AFTER))
    private void renderBraidDisplays(GpuBufferSlice gpuBufferSlice, LevelRenderState levelRenderState, ProfilerFiller profiler, Matrix4f matrix4f, ResourceHandle<?> handle, ResourceHandle<?> handle2, boolean bl, ResourceHandle<?> handle3, ResourceHandle<?> handle4, CallbackInfo ci, @Local PoseStack matrixStack) {
        BraidDisplayBinding.renderAutomaticDisplays(matrixStack, levelRenderState.cameraRenderState, submitNodeStorage);
    }
}
