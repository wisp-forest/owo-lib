package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.gl.GlCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlCommandEncoder.class)
public interface GlCommandEncoderAccessor {

    @Accessor("renderPassOpen")
    void owo$setRenderPassOpen(boolean open);
}
