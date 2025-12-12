package io.wispforest.owo.mixin.ui.access;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {
    @Accessor("shaderLightDirections")
    static GpuBufferSlice owo$getShaderLightDirections() {
        throw new UnsupportedOperationException();
    }
}
