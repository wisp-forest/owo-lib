package com.glisco.owo.registration.reflect;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

public interface BlockRegistryContainer extends AutoRegistryContainer<Block> {
    @Override
    default Registry<Block> getRegistry() {
        return Registry.BLOCK;
    }

    @Override
    default Class<Block> getTargetFieldType() {
        return Block.class;
    }
}
