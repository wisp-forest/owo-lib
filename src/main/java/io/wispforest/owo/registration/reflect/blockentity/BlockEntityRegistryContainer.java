package io.wispforest.owo.registration.reflect.blockentity;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.entity.EntityRegistryEntry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.Supplier;

public abstract class BlockEntityRegistryContainer extends AutoRegistryContainer<BlockEntityType<?>> {

    @Override
    public final Registry<BlockEntityType<?>> getRegistry() {
        return Registries.BLOCK_ENTITY_TYPE;
    }

    @Override
    public final Class<BlockEntityType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(BlockEntityType.class);
    }

    public static <BE extends BlockEntity> BlockEntityRegistryEntry<BE> blockEntity(Supplier<BlockEntityType<BE>> supplier) {
        return new BlockEntityRegistryEntry<>(supplier);
    }
}
