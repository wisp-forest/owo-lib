package com.glisco.owo.registration.reflect;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public interface ItemRegistryContainer extends AutoRegistryContainer<Item> {
    @Override
    default Registry<Item> getRegistry() {
        return Registry.ITEM;
    }

    @Override
    default Class<Item> getTargetFieldType() {
        return Item.class;
    }
}
