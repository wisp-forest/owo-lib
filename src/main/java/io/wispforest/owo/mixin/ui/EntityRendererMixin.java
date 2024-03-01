package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void cancelLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (((OwoEntityRenderDispatcherExtension) this.dispatcher).owo$showNametag()) return;
        ci.cancel();
    }

    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", shift = At.Shift.AFTER))
    private void adjustLabelRotation(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (!((OwoEntityRenderDispatcherExtension) this.dispatcher).owo$counterRotate()) return;

        matrices.multiply(new Quaternionf(this.dispatcher.getRotation()).invert());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
    }

}
