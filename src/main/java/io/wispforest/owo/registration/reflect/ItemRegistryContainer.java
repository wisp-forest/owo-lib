package io.wispforest.owo.registration.reflect;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface ItemRegistryContainer extends AutoRegistryContainer<Item> {
    @Override
    default Registry<Item> getRegistry() {
        return Registries.ITEM;
    }

    @Override
    default Class<Item> getTargetFieldType() {
        return Item.class;
    }
}
