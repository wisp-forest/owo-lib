package io.wispforest.owo.registration.reflect;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public abstract class BlockEntityRegistryContainer extends AutoRegistryContainer<BlockEntityType<?>> {

    @Override
    public final Registry<BlockEntityType<?>> getRegistry() {
        return Registries.BLOCK_ENTITY_TYPE;
    }

    @Override
    public final Class<BlockEntityType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(BlockEntityType.class);
    }
}
