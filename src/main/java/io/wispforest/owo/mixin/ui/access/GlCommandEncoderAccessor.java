package io.wispforest.owo.mixin.ui.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public interface GlCommandEncoderAccessor {

    // FIXME 26.2: inRenderPass field removed. Blur rendering uses this as a workaround to
    // copy textures outside a render pass. Needs a new approach for 26.2 rendering pipeline.
    // @Accessor("inRenderPass")
    // void owo$setInRenderPass(boolean open);
}
