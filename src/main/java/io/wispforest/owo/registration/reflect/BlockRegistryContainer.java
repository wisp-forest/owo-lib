package io.wispforest.owo.registration.reflect;

import io.wispforest.owo.registration.annotations.AssignedName;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public interface BlockRegistryContainer extends AutoRegistryContainer<Block> {

    @Override
    default Registry<Block> getRegistry() {
        return Registries.BLOCK;
    }

    @Override
    default Class<Block> getTargetFieldType() {
        return Block.class;
    }

    @Override
    default void postProcessField(String namespace, Block value, String identifier, Field field) {
        if (field.isAnnotationPresent(NoBlockItem.class)) return;
        Registry.register(Registries.ITEM, new Identifier(namespace, identifier), createBlockItem(value, identifier));
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

    /**
     * Declares that the annotated field should not
     * have a block item created for it
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NoBlockItem {}
}
