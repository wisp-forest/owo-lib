package io.wispforest.owo.mixin.braid;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Provides the renderer backend for additional SDL windows. Surface context
 * management now belongs to RenderPearl, replacing GLFW's context-share hook. */
@Mixin(RenderSystem.class)
public interface WindowMixin {
    @Accessor("BACKEND")
    static GpuBackend owo$getBackend() {
        throw new UnsupportedOperationException();
    }
}
