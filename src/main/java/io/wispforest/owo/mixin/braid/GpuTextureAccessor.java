package io.wispforest.owo.mixin.braid;

import com.mojang.blaze3d.textures.FilterMode;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(com.mojang.blaze3d.textures.GpuTexture.class)
public interface GpuTextureAccessor {
    @Accessor("minFilter")
    FilterMode owo$getMinFilter();

    @Accessor("magFilter")
    FilterMode owo$getMagFilter();

    @Accessor("useMipmaps")
    boolean owo$getUseMipmaps();
}
