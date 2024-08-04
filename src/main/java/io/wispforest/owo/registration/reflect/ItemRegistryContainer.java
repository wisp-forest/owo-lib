package io.wispforest.owo.registration.reflect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public interface ItemRegistryContainer extends AutoRegistryContainer<Item> {
    @Override
    default Registry<Item> getRegistry() {
        return BuiltInRegistries.ITEM;
    }

    @Override
    default Class<Item> getTargetFieldType() {
        return Item.class;
    }
}
