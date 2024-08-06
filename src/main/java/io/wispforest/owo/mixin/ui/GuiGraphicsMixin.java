package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.util.pond.OwoTessellatorExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @SuppressWarnings("ConstantValue")
    @Inject(method = "drawTexturedQuad(Lnet/minecraft/resources/Identifier;IIIIIFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tessellator;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectBufferBegin(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, CallbackInfo ci, Matrix4f matrix4f) {
        if (!((Object) this instanceof OwoUIDrawContext context) || !context.recording()) return;

        ((OwoTessellatorExtension) Tessellator.getInstance()).owo$skipNextBegin();
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "drawTexturedQuad(Lnet/minecraft/resources/Identifier;IIIIIFFFFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tessellator;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectBufferBeginPartTwo(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha, CallbackInfo ci, Matrix4f matrix4f) {
        if (!((Object) this instanceof OwoUIDrawContext context) || !context.recording()) return;

        ((OwoTessellatorExtension) Tessellator.getInstance()).owo$skipNextBegin();
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "drawTexturedQuad(Lnet/minecraft/resources/Identifier;IIIIIFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;buildOrThrow()Lcom/mojang/blaze3d/vertex/MeshData;"), cancellable = true)
    private void skipDraw(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, CallbackInfo ci, @Local(ordinal = 0) BufferBuilder builder) {
        if ((Object) this instanceof OwoUIDrawContext context && context.recording()) {
            ci.cancel();
            ((OwoTessellatorExtension) Tessellator.getInstance()).owo$setStoredBuilder(builder);
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "drawTexturedQuad(Lnet/minecraft/resources/Identifier;IIIIIFFFFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;buildOrThrow()Lcom/mojang/blaze3d/vertex/MeshData;"), cancellable = true)
    private void skipDrawSeason2(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha, CallbackInfo ci, @Local(ordinal = 0) BufferBuilder builder) {
        if ((Object) this instanceof OwoUIDrawContext context && context.recording()) {
            ci.cancel();
            ((OwoTessellatorExtension) Tessellator.getInstance()).owo$setStoredBuilder(builder);
        }
    }
}
