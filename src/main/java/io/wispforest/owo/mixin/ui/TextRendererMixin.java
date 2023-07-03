package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.util.pond.OwoTextRendererExtension;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TextRenderer.class)
public class TextRendererMixin implements OwoTextRendererExtension {

    private VertexConsumerProvider.Immediate owo$labelVertexConsumers = null;

    @Override
    public void owo$beginCache() {
        this.owo$labelVertexConsumers = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    }

    @ModifyArg(method = "draw(Ljava/lang/String;FFILorg/joml/Matrix4f;ZZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I"))
    private VertexConsumerProvider injectConsumers(VertexConsumerProvider immediate) {
        if (this.owo$labelVertexConsumers == null) return immediate;
        return this.owo$labelVertexConsumers;
    }

    @ModifyArg(method = "draw(Lnet/minecraft/text/OrderedText;FFILorg/joml/Matrix4f;Z)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"))
    private VertexConsumerProvider injectConsumers2(VertexConsumerProvider immediate) {
        if (this.owo$labelVertexConsumers == null) return immediate;
        return this.owo$labelVertexConsumers;
    }

    @Inject(method = "draw(Ljava/lang/String;FFILorg/joml/Matrix4f;ZZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I", shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void skipDraw(String text, float x, float y, int color, Matrix4f matrix, boolean shadow, boolean mirror, CallbackInfoReturnable<Integer> cir, VertexConsumerProvider.Immediate immediate, int i) {
        if (this.owo$labelVertexConsumers == null) return;
        cir.setReturnValue(i);
    }

    @Inject(method = "draw(Lnet/minecraft/text/OrderedText;FFILorg/joml/Matrix4f;Z)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void skipDraw2(OrderedText text, float x, float y, int color, Matrix4f matrix, boolean shadow, CallbackInfoReturnable<Integer> cir, VertexConsumerProvider.Immediate immediate, int i) {
        if (this.owo$labelVertexConsumers == null) return;
        cir.setReturnValue(i);
    }

    @Override
    public void owo$submitCache() {
        this.owo$labelVertexConsumers.draw();
        this.owo$labelVertexConsumers = null;
    }

}
