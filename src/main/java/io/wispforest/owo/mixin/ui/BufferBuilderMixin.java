package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.util.pond.OwoBufferBuilderExtension;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements OwoBufferBuilderExtension {

    @Unique
    private boolean owo$skipBegin = false;

    @Inject(method = "begin", at = @At("HEAD"), cancellable = true)
    private void skipBegin(VertexFormat.DrawMode drawMode, VertexFormat format, CallbackInfo ci) {
        if (!this.owo$skipBegin) return;

        this.owo$skipBegin = false;
        ci.cancel();
    }

    @Override
    public void owo$skipNextBegin() {
        this.owo$skipBegin = true;
    }
}
