package io.wispforest.owo.mixin.ui.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.StagedVertexBuffer$Draw")
public interface StagedVertexBufferDrawAccessor {

    @Accessor("indexCount")
    int owo$getIndexCount();
}
