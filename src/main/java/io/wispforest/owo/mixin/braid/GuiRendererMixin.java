package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.wispforest.owo.braid.core.BraidRenderPipelines;
import io.wispforest.owo.braid.util.BraidGuiRenderer;
import io.wispforest.owo.util.pond.BraidGuiRendererExtension;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.ProjectionMatrix2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin implements BraidGuiRendererExtension {

    @Unique
    private BraidGuiRenderer.Target target = null;

    @Override
    public void owo$setTarget(BraidGuiRenderer.Target target) {
        this.target = target;
    }

    // ---

    @WrapOperation(method = "renderPreparedDraws", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/ProjectionMatrix2;set(FF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
    private GpuBufferSlice injectSurfaceDimensions(ProjectionMatrix2 instance, float width, float height, Operation<GpuBufferSlice> original) {
        if (this.target == null) return original.call(instance, width, height);

        var surface = this.target.surface();
        return original.call(instance, (float) surface.width(), (float) surface.height());
    }

    @ModifyExpressionValue(method = "renderPreparedDraws", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;"))
    private Framebuffer injectFramebuffer(Framebuffer original) {
        if (this.target == null) return original;
        return this.target.framebuffer();
    }

    @ModifyExpressionValue(method = "enableScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getFramebufferHeight()I"))
    private int injectSurfaceHeightForScissor(int original) {
        if (this.target == null) return original;
        return this.target.framebuffer().textureHeight;
    }

    @ModifyExpressionValue(method = "enableScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getScaleFactor()I"))
    private int injectSurfaceScaleForScissor(int original) {
        if (this.target == null) return original;
        return (int) this.target.surface().scaleFactor();
    }

    @ModifyExpressionValue(method = "prepareSpecialElements", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getScaleFactor()I"))
    private int injectSurfaceScaleForPIP(int original) {
        if (this.target == null) return original;
        return (int) this.target.surface().scaleFactor();
    }

    @ModifyExpressionValue(method = "getWindowScaleFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getScaleFactor()I"))
    private int injectSurfaceScaleForItemAtlas(int original) {
        if (this.target == null) return original;
        return (int) this.target.surface().scaleFactor();
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

    // ---

    @ModifyExpressionValue(method = "close", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/render/GuiRenderer;specialElementRenderers:Ljava/util/Map;"))
    private Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> keepAliveRenderers(Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> original) {
        if (((Object) this) instanceof BraidGuiRenderer) {
            return Map.of();
        }

        return original;
    }
}
