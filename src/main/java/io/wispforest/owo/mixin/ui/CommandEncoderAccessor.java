package io.wispforest.owo.mixin.ui;

import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(com.mojang.renderpearl.frontend.FrontendCommandEncoder.class)
public interface CommandEncoderAccessor {
    @Accessor("backend")
    CommandEncoderBackend owo$getBackend();
}
