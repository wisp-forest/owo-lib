package io.wispforest.owo.registration.reflect.item;

import io.wispforest.owo.registration.reflect.entry.MemoizedRegistryEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

import java.util.function.Supplier;

public final class ItemRegistryEntry<T extends Item> extends MemoizedRegistryEntry<T, Item> implements ItemConvertible {
    public ItemRegistryEntry(Supplier<T> factory) {
        super(factory);
    }

    @Override
    public Item asItem() {
        return this.value();
    }
}
