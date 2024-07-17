package io.wispforest.owo.registration.reflect;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.function.Supplier;

public abstract class ItemRegistryContainer extends AutoRegistryContainer<Item> {

    @Override
    public final Registry<Item> getRegistry() {
        return Registries.ITEM;
    }

    @Override
    public final Class<Item> getTargetFieldType() {
        return Item.class;
    }
}
