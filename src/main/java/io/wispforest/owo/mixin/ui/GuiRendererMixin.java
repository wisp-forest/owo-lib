package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.wispforest.owo.mixin.ui.access.GlCommandEncoderAccessor;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;
import io.wispforest.owo.ui.renderstate.OwoSpecialElementRenderState;
import io.wispforest.owo.ui.renderstate.OwoSpecialRendererAllocator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.HashMap;
import java.util.Map;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Shadow
    @Final
    private VertexConsumerProvider.Immediate vertexConsumers;

    @Shadow
    @Nullable
    private TextureSetup textureSetup;

    @Unique
    private final Map<Class<? extends OwoSpecialElementRenderState<?>>, OwoSpecialRendererAllocator> allocators = new HashMap<>();

    @ModifyVariable(method = "prepareSpecialElement", at = @At("STORE"))
    private SpecialGuiElementRenderer injectRenderer(SpecialGuiElementRenderer original, @Local(argsOnly = true) SpecialGuiElementRenderState elementState) {
        if (!(elementState instanceof OwoSpecialElementRenderState<?> owoRenderState)) return original;
        return this.allocators.computeIfAbsent((Class<? extends OwoSpecialElementRenderState<?>>) owoRenderState.getClass(), $ -> new OwoSpecialRendererAllocator()).alloc(owoRenderState, this.vertexConsumers);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At("TAIL"))
    private void resetAllocators(GpuBufferSlice fogBuffer, CallbackInfo ci) {
        this.allocators.values().forEach(OwoSpecialRendererAllocator::reset);
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void closeAllocators(CallbackInfo ci) {
        this.allocators.values().forEach(OwoSpecialRendererAllocator::close);
    }

    @ModifyArgs(
        method = "render(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setIndexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V")
    )
    private void fixNonQuadIndexing(Args args, @Local(argsOnly = true) GuiRenderer.Draw draw) {
        var pipeline = draw.pipeline();
        if (!pipeline.getLocation().getNamespace().equals("owo")) return;

        if (pipeline.getVertexFormatMode() != VertexFormat.DrawMode.QUADS) {
            var shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            args.set(0, shapeIndexBuffer.getIndexBuffer(draw.indexCount()));
            args.set(1, shapeIndexBuffer.getIndexType());
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V")
    )
    private void drawBlur(GuiRenderer.Draw draw, RenderPass pass, GpuBuffer indexBuffer, VertexFormat.IndexType indexType, CallbackInfo ci) {
        var blurSetup = BlurQuadElementRenderState.getBlurSetupOf(draw.textureSetup());
        if (blurSetup == null) return;

        var mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        var inputSize = new Vector2i(mainBuffer.textureWidth, mainBuffer.textureHeight);

        var encoder = RenderSystem.getDevice().createCommandEncoder();

        ((GlCommandEncoderAccessor)encoder).owo$setRenderPassOpen(false);
        encoder.copyTextureToTexture(
            MinecraftClient.getInstance().getFramebuffer().getColorAttachment(),
            BlurQuadElementRenderState.input.getColorAttachment(),
            0, 0, 0, 0, 0, inputSize.x, inputSize.y
        );

        var uniforms = BlurQuadElementRenderState.uniforms.write(inputSize, blurSetup.directions(), blurSetup.quality(), blurSetup.size());
        ((GlCommandEncoderAccessor)encoder).owo$setRenderPassOpen(true);

        pass.setUniform("BlurSettings", uniforms);
        pass.bindSampler("InputSampler", BlurQuadElementRenderState.inputView);
    }

    @ModifyExpressionValue(method = "prepareSimpleElement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureSetup;equals(Ljava/lang/Object;)Z"))
    private boolean adjustCheckForBlurElements(boolean original, @Local(argsOnly = true) SimpleGuiElementRenderState state) {
        return original && !(state instanceof BlurQuadElementRenderState || BlurQuadElementRenderState.hasBlurSetupFor(textureSetup));
    }
}
