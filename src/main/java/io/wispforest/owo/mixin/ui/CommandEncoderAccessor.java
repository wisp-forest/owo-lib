package io.wispforest.owo.mixin.ui;

import com.mojang.blaze3d.systems.CommandEncoderBackend;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(com.mojang.blaze3d.systems.CommandEncoder.class)
public interface CommandEncoderAccessor {
    @Accessor("backend")
    CommandEncoderBackend owo$getBackend();
}
