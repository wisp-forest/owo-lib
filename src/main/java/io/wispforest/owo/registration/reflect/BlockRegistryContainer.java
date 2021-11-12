package io.wispforest.owo.registration.reflect;

import io.wispforest.owo.registration.annotations.AssignedName;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
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

    @Override
    default void postProcessField(String namespace, Block value, String identifier) {
        Registry.register(Registry.ITEM, new Identifier(namespace, identifier), createBlockItem(value, identifier));
    }

    /**
     * Creates a block item for the given block
     *
     * @param block      The block to create an item for
     * @param identifier The identifier the field was assigned, possibly overridden by an {@link AssignedName}
     *                   annotation and always fully lowercase
     * @return The created BlockItem instance
     */
    default BlockItem createBlockItem(Block block, String identifier) {
        return new BlockItem(block, new Item.Settings());
    }
}
