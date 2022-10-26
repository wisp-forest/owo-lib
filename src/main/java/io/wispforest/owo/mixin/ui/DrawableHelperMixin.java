package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.util.pond.OwoBufferBuilderExtension;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DrawableHelper.class)
public class DrawableHelperMixin {

    @Inject(method = "drawTexturedQuad", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;begin(Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void injectBufferBegin(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, CallbackInfo ci, BufferBuilder bufferBuilder) {
        if (!Drawer.recording()) return;

        if (bufferBuilder.isBuilding()) {
            ((OwoBufferBuilderExtension) bufferBuilder).owo$skipNextBegin();
        }
    }

    @Inject(method = "drawTexturedQuad", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;end()Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;"), cancellable = true)
    private static void skipDraw(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, CallbackInfo ci) {
        if (Drawer.recording()) ci.cancel();
    }
}
