package io.wispforest.owo.mixin.ui.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public interface GlCommandEncoderAccessor {

    @Accessor("inRenderPass")
    void owo$setInRenderPass(boolean open);
}
