package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.util.pond.OwoImmediateExtension;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(VertexConsumerProvider.Immediate.class)
public abstract class ImmediateMixin implements OwoImmediateExtension {

    @Unique
    private final Set<RenderLayer> owo$batchedLayers = new HashSet<>();

    @WrapOperation(method = "getBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;areVerticesNotShared()Z"))
    private boolean owo$shouldReuseBuffer(RenderLayer instance, Operation<Boolean> original) {
        if (owo$batchedLayers.contains(instance)) return true;

        return original.call(instance);
    }

    @Inject(method = "draw(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/BufferBuilder;)V", at = @At("HEAD"))
    private void owo$removeBatchedLayer(RenderLayer layer, BufferBuilder builder, CallbackInfo ci) {
        this.owo$batchedLayers.remove(layer);
    }

    @Override
    public void owo$addBatchedLayer(RenderLayer renderLayer) {
        this.owo$batchedLayers.add(renderLayer);
    }
}
