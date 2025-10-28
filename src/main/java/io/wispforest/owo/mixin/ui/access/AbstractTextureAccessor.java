package io.wispforest.owo.mixin.ui.access;

import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.texture.AbstractTexture.class)
public interface AbstractTextureAccessor {
    @Accessor("bilinear")
    boolean owo$getBilinear();
    @Accessor("mipmap")
    boolean owo$getMipmap();
}
