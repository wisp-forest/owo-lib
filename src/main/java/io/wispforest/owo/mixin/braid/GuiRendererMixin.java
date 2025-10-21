package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.wispforest.owo.braid.core.BraidRenderPipelines;
import io.wispforest.owo.braid.util.BraidGuiRendererTargetOverride;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Shadow
    private @Nullable RenderPipeline pipeline;

    @Shadow
    private @Nullable TextureSetup textureSetup;
    @WrapOperation(method = "renderPreparedDraws", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/ProjectionMatrix2;set(FF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
    private GpuBufferSlice injectSurfaceDimensions(ProjectionMatrix2 instance, float width, float height, Operation<GpuBufferSlice> original) {
        if (BraidGuiRendererTargetOverride.current() == null) return original.call(instance, width, height);

        var surface = BraidGuiRendererTargetOverride.current().surface();
        return original.call(instance, (float) surface.width(), (float) surface.height());
    }

    @ModifyExpressionValue(method = "renderPreparedDraws", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;"))
    private Framebuffer injectFramebuffer(Framebuffer original) {
        if (BraidGuiRendererTargetOverride.current() == null) return original;
        return BraidGuiRendererTargetOverride.current().framebuffer();
    }

    @ModifyExpressionValue(method = "enableScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getFramebufferHeight()I"))
    private int injectSurfaceHeightForScissor(int original) {
        if (BraidGuiRendererTargetOverride.current() == null) return original;
        return BraidGuiRendererTargetOverride.current().framebuffer().textureHeight;
    }

    @ModifyExpressionValue(method = "enableScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getScaleFactor()I"))
    private int injectSurfaceScaleForScissor(int original) {
        if (BraidGuiRendererTargetOverride.current() == null) return original;
        return (int) BraidGuiRendererTargetOverride.current().surface().scaleFactor();
    }

    @ModifyExpressionValue(method = "prepareSpecialElements", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getScaleFactor()I"))
    private int injectSurfaceScaleForPIP(int original) {
        if (BraidGuiRendererTargetOverride.current() == null) return original;
        return (int) BraidGuiRendererTargetOverride.current().surface().scaleFactor();
    }

    // ---

    @Inject(method = "render(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V", at = @At(value = "HEAD"))
    private void setupTextureFilter(GuiRenderer.Draw draw, RenderPass pass, GpuBuffer indexBuffer, VertexFormat.IndexType indexType, CallbackInfo ci, @Share("minFilter") LocalRef<FilterMode> minFilter, @Share("magFilter") LocalRef<FilterMode> magFilter) {
        if (draw.textureSetup().texure0() == null) {
            return;
        }

        var texture = draw.textureSetup().texure0().texture();
        var textureAccess = (GpuTextureAccessor) texture;

        if (draw.pipeline() == BraidRenderPipelines.TEXTURED_BILINEAR) {
            minFilter.set(textureAccess.owo$getMinFilter());
            magFilter.set(textureAccess.owo$getMagFilter());

            texture.setTextureFilter(FilterMode.LINEAR, FilterMode.LINEAR, textureAccess.owo$getUseMipmaps());
        } else if (draw.pipeline() == BraidRenderPipelines.TEXTURED_NEAREST) {
            minFilter.set(textureAccess.owo$getMinFilter());
            magFilter.set(textureAccess.owo$getMagFilter());

            texture.setTextureFilter(FilterMode.NEAREST, FilterMode.NEAREST, textureAccess.owo$getUseMipmaps());
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V", at = @At("TAIL"))
    private void resetTextureFilter(GuiRenderer.Draw draw, RenderPass pass, GpuBuffer indexBuffer, VertexFormat.IndexType indexType, CallbackInfo ci, @Share("minFilter") LocalRef<FilterMode> minFilter, @Share("magFilter") LocalRef<FilterMode> magFilter) {
        if (minFilter.get() != null && magFilter.get() != null) {
            var texture = draw.textureSetup().texure0().texture();
            var textureAccess = (GpuTextureAccessor) texture;

            texture.setTextureFilter(minFilter.get(), magFilter.get(), textureAccess.owo$getUseMipmaps());
        }
    }
}
