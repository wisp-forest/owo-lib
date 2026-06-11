package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import io.wispforest.owo.braid.core.BraidRenderPipelines;
import io.wispforest.owo.braid.util.BraidGuiRenderer;
import io.wispforest.owo.util.pond.BraidGuiRendererExtension;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;setupOrtho(FFFFZ)V"))
    private void injectSurfaceDimensions(Projection instance, float zNear, float zFar, float width, float height, boolean invertY, Operation<Void> original) {
        if (this.target == null) {
            original.call(instance, zNear, zFar, width, height, invertY);
            return;
        }

        var surface = this.target.surface();
        original.call(instance, zNear, zFar, (float) surface.width(), (float) surface.height(), invertY);
    }

    @ModifyExpressionValue(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget injectFramebuffer(RenderTarget original) {
        if (this.target == null) return original;
        return this.target.framebuffer();
    }

    @ModifyExpressionValue(method = "enableScissor", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WindowRenderState;height:I", opcode = Opcodes.GETFIELD))
    private int injectSurfaceHeightForScissor(int original) {
        if (this.target == null) return original;
        return this.target.framebuffer().height;
    }

    @ModifyExpressionValue(method = "enableScissor", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WindowRenderState;guiScale:I", opcode = Opcodes.GETFIELD))
    private int injectSurfaceScaleForScissor(int original) {
        if (this.target == null) return original;
        return (int) this.target.surface().scaleFactor();
    }

    @ModifyExpressionValue(method = "preparePictureInPicture", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WindowRenderState;guiScale:I", opcode = Opcodes.GETFIELD))
    private int injectSurfaceScaleForPIP(int original) {
        if (this.target == null) return original;
        return (int) this.target.surface().scaleFactor();
    }

    @ModifyExpressionValue(method = "getGuiScaleInvalidatingItemAtlasIfChanged", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WindowRenderState;guiScale:I", opcode = Opcodes.GETFIELD))
    private int injectSurfaceScaleForItemAtlas(int original) {
        if (this.target == null) return original;
        return (int) this.target.surface().scaleFactor();
    }

    // ---

    @ModifyArg(
        method = "executeDraw(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;bindTexture(Ljava/lang/String;Lcom/mojang/blaze3d/textures/GpuTextureView;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal = 0),
        index = 2
    )
    private @Nullable GpuSampler injectTextureFilter(GpuSampler sampler, @Local(argsOnly = true) GuiRenderer.Draw draw) {
        if (draw.textureSetup().texure0() == null) {
            return sampler;
        }

        if (draw.pipeline() == BraidRenderPipelines.TEXTURED_BILINEAR) {
            return RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.LINEAR, false);
        } else if (draw.pipeline() == BraidRenderPipelines.TEXTURED_NEAREST) {
            return RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.NEAREST, FilterMode.NEAREST, false);
        } else {
            return sampler;
        }
    }

    // ---

    @ModifyExpressionValue(method = "close", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/render/GuiRenderer;pictureInPictureRendererPools:Ljava/util/Map;"))
    private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> keepAliveRenderers(Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> original) {
        if (((Object) this) instanceof BraidGuiRenderer) {
            return Map.of();
        }

        return original;
    }
}
