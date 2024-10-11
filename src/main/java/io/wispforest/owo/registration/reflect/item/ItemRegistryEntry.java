package io.wispforest.owo.registration.reflect.item;

import io.wispforest.owo.registration.reflect.entry.MemoizedRegistryEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.RegistryKey;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ItemRegistryEntry<T extends Item> extends MemoizedRegistryEntry<T, Item> implements ItemConvertible {

    private final Item.Settings settings;

    public ItemRegistryEntry(Function<Item.Settings, T> factory, Item.Settings settings) {
        super(() -> factory.apply(settings));

        this.settings = settings;
    }

    @Override
    public void setup(RegistryKey<Item> registryKey) {
        this.settings.registryKey(registryKey);
    }

    @Override
    public Item asItem() {
        return this.value();
    }
}
