package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.wispforest.owo.mixin.ui.access.GlCommandEncoderAccessor;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Shadow
    @Nullable
    private TextureSetup previousTextureSetup;

    @ModifyArgs(
        method = "executeDraw(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setIndexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V")
    )
    private void fixNonQuadIndexing(Args args, @Local(argsOnly = true) GuiRenderer.Draw draw) {
        var pipeline = draw.pipeline();
        if (!pipeline.getLocation().getNamespace().equals("owo")) return;

        if (pipeline.getVertexFormatMode() != VertexFormat.Mode.QUADS) {
            var shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            args.set(0, shapeIndexBuffer.getBuffer(draw.indexCount()));
            args.set(1, shapeIndexBuffer.type());
        }
    }

    @Inject(
        method = "executeDraw(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V")
    )
    private void drawBlur(GuiRenderer.Draw draw, RenderPass pass, GpuBuffer indexBuffer, VertexFormat.IndexType indexType, CallbackInfo ci) {
        var blurSetup = BlurQuadElementRenderState.getBlurSetupOf(draw.textureSetup());
        if (blurSetup == null) return;

        var mainBuffer = Minecraft.getInstance().getMainRenderTarget();
        var inputSize = new Vector2i(mainBuffer.width, mainBuffer.height);

        var encoder = RenderSystem.getDevice().createCommandEncoder();

        ((GlCommandEncoderAccessor)encoder).owo$setInRenderPass(false);
        encoder.copyTextureToTexture(
            Minecraft.getInstance().getMainRenderTarget().getColorTexture(),
            BlurQuadElementRenderState.input.getColorTexture(),
            0, 0, 0, 0, 0, inputSize.x, inputSize.y
        );

        var uniforms = BlurQuadElementRenderState.uniforms.write(inputSize, blurSetup.directions(), blurSetup.quality(), blurSetup.size());
        ((GlCommandEncoderAccessor)encoder).owo$setInRenderPass(true);

        pass.setUniform("BlurSettings", uniforms);
        pass.bindTexture("InputSampler", BlurQuadElementRenderState.inputView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
    }

    @ModifyExpressionValue(method = "addElementToMesh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/TextureSetup;equals(Ljava/lang/Object;)Z"))
    private boolean adjustCheckForBlurElements(boolean original, @Local(argsOnly = true) GuiElementRenderState state) {
        return original && !(state instanceof BlurQuadElementRenderState || BlurQuadElementRenderState.hasBlurSetupFor(previousTextureSetup));
    }
}
