package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.util.pond.OwoBufferBuilderExtension;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DrawContext.class)
public class DrawContextMixin {

    @SuppressWarnings("ConstantValue")
    @Inject(method = "drawTexturedQuad(Lnet/minecraft/util/Identifier;IIIIIFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;begin(Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectBufferBegin(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, CallbackInfo ci, Matrix4f matrix4f, BufferBuilder bufferBuilder) {
        if (!((Object) this instanceof OwoUIDrawContext context) || !context.recording()) return;

        if (bufferBuilder.isBuilding()) {
            ((OwoBufferBuilderExtension) bufferBuilder).owo$skipNextBegin();
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "drawTexturedQuad(Lnet/minecraft/util/Identifier;IIIIIFFFFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;begin(Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectBufferBeginPartTwo(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha, CallbackInfo ci, Matrix4f matrix4f, BufferBuilder bufferBuilder) {
        if (!((Object) this instanceof OwoUIDrawContext context) || !context.recording()) return;

        if (bufferBuilder.isBuilding()) {
            ((OwoBufferBuilderExtension) bufferBuilder).owo$skipNextBegin();
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "drawTexturedQuad(Lnet/minecraft/util/Identifier;IIIIIFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;end()Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;"), cancellable = true)
    private void skipDraw(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, CallbackInfo ci) {
        if ((Object) this instanceof OwoUIDrawContext context && context.recording()) ci.cancel();
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "drawTexturedQuad(Lnet/minecraft/util/Identifier;IIIIIFFFFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;end()Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;"), cancellable = true)
    private void skipDrawSeason2(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha, CallbackInfo ci) {
        if ((Object) this instanceof OwoUIDrawContext context && context.recording()) ci.cancel();
    }
}
