package io.wispforest.owo.registration.reflect;

import io.wispforest.owo.registration.annotations.AssignedName;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface BlockRegistryContainer extends AutoRegistryContainer<Block> {

    @Override
    default Registry<Block> getRegistry() {
        return BuiltInRegistries.BLOCK;
    }

    @Override
    default Class<Block> getTargetFieldType() {
        return Block.class;
    }

    @Override
    default void postProcessField(String namespace, Block value, String identifier, Field field) {
        if (field.isAnnotationPresent(NoBlockItem.class)) return;
        Registry.register(BuiltInRegistries.ITEM, Identifier.of(namespace, identifier), createBlockItem(value, identifier));
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
        return new BlockItem(block, new Item.Properties());
    }

    /**
     * Declares that the annotated field should not
     * have a block item created for it
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NoBlockItem {}
}
