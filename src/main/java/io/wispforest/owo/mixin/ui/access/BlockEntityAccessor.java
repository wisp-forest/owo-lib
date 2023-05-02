package io.wispforest.owo.mixin.ui.access;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntity.class)
public interface BlockEntityAccessor {
    @Accessor("cachedState")
    void owo$setCachedState(BlockState state);
}
