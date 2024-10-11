package io.wispforest.owo.registration.reflect.blockentity;

import io.wispforest.owo.registration.reflect.entry.MemoizedRegistryEntry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;

import java.util.function.Supplier;

public class BlockEntityRegistryEntry<BE extends BlockEntity> extends MemoizedRegistryEntry<BlockEntityType<BE>, BlockEntityType<?>> {
    public BlockEntityRegistryEntry(Supplier<BlockEntityType<BE>> factory) {
        super(factory);
    }
}
