package io.wispforest.owo.registration.reflect.item;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.Function;
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

    public static <T extends Item> ItemRegistryEntry<T> item(Function<Item.Settings, T> factory) {
        return item(factory, new Item.Settings());
    }

    public static <T extends Item> ItemRegistryEntry<T> item(Function<Item.Settings, T> factory, Item.Settings settings) {
        return new ItemRegistryEntry<>(factory, settings);
    }
}
