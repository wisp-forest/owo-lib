package io.wispforest.owo.registration.reflect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface BlockEntityRegistryContainer extends AutoRegistryContainer<BlockEntityType<?>> {

    @Override
    default Registry<BlockEntityType<?>> getRegistry() {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE;
    }

    @Override
    default Class<BlockEntityType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(BlockEntityType.class);
    }
}
