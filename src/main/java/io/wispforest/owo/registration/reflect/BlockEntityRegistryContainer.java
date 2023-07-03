package io.wispforest.owo.registration.reflect;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface BlockEntityRegistryContainer extends AutoRegistryContainer<BlockEntityType<?>> {

    @Override
    default Registry<BlockEntityType<?>> getRegistry() {
        return Registries.BLOCK_ENTITY_TYPE;
    }

    @Override
    default Class<BlockEntityType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(BlockEntityType.class);
    }
}
