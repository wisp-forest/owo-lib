package io.wispforest.owo.mixin.ui;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.wispforest.owo.mixin.BufferBuilderAccessor;
import io.wispforest.owo.util.pond.OwoTessellatorExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Tessellator.class)
public class TessellatorMixin implements OwoTessellatorExtension {

    @Unique
    private boolean owo$skipBegin = false;

    @Unique
    @Nullable
    private BufferBuilder bufferBuilder = null;

    @Inject(method = "begin", at = @At("HEAD"), cancellable = true)
    private void skipBegin(VertexFormat.Mode drawMode, VertexFormat format, CallbackInfoReturnable<BufferBuilder> cir) {
        if(this.bufferBuilder == null) return;

        var bl = this.owo$skipBegin
                && builderAccessor().getMode().equals(drawMode)
                && builderAccessor().getFormat().equals(format);

        if (!bl) return;

        this.owo$skipBegin = false;

        cir.setReturnValue(this.bufferBuilder);

        this.bufferBuilder = null;
    }

    @Override
    public void owo$skipNextBegin() {
        if(this.bufferBuilder != null && builderAccessor().isBuilding()) this.owo$skipBegin = true;
    }

    @Override
    public void owo$setStoredBuilder(BufferBuilder builder) {
        this.bufferBuilder = builder;
    }

    @Override
    public BufferBuilder owo$getStoredBuilder() {
        return this.bufferBuilder;
    }

    @Unique
    private BufferBuilderAccessor builderAccessor() {
        return (BufferBuilderAccessor) this.bufferBuilder;
    }
}
