package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Shadow
    @Nullable
    private TextureSetup previousTextureSetup;

    @Shadow
    @Final
    private List<GuiRenderer.Draw> draws;

    @WrapMethod(method = "executeDrawRange")
    private void splitAtBlur(Supplier<String> label, RenderTarget mainRenderTarget, GpuBufferSlice dynamicTransforms, int startIndex, int endIndex, Operation<Void> original) {
        int segStart = startIndex;
        for (int i = startIndex; i < endIndex; i++) {
            if (BlurQuadElementRenderState.getBlurSetupOf(this.draws.get(i).textureSetup()) != null) {
                if (i > segStart) {
                    original.call(label, mainRenderTarget, dynamicTransforms, segStart, i);
                }

                var size = new Vector2i(mainRenderTarget.width, mainRenderTarget.height);
                RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                    mainRenderTarget.getColorTexture(),
                    BlurQuadElementRenderState.input.getColorTexture(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    size.x,
                    size.y
                );

                original.call(label, mainRenderTarget, dynamicTransforms, i, i + 1);
                segStart = i + 1;
            }
        }

        original.call(label, mainRenderTarget, dynamicTransforms, segStart, endIndex);
    }

    @Inject(
        method = "executeDraw(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIIII)V")
    )
    private void drawBlur(GuiRenderer.Draw draw, RenderPass renderPass, CallbackInfo ci) {
        var blurSetup = BlurQuadElementRenderState.getBlurSetupOf(draw.textureSetup());
        if (blurSetup == null) return;

        var mainBuffer = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        var inputSize = new Vector2i(mainBuffer.width, mainBuffer.height);

        var uniforms = BlurQuadElementRenderState.uniforms.write(inputSize, blurSetup.directions(), blurSetup.quality(), blurSetup.size());

        renderPass.setUniform("BlurSettings", uniforms);
        renderPass.bindTexture("InputSampler", BlurQuadElementRenderState.inputView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
    }

    @ModifyExpressionValue(method = "addElementToMesh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/TextureSetup;equals(Ljava/lang/Object;)Z"))
    private boolean adjustCheckForBlurElements(boolean original, @Local(argsOnly = true) GuiElementRenderState elementState) {
        return original && !(elementState instanceof BlurQuadElementRenderState || BlurQuadElementRenderState.hasBlurSetupFor(previousTextureSetup));
    }
}
