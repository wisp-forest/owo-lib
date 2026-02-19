package io.wispforest.owo.mixin.ui.access;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlCommandEncoder.class)
public interface GlCommandEncoderAccessor {

    @Accessor("inRenderPass")
    void owo$setInRenderPass(boolean open);
}
