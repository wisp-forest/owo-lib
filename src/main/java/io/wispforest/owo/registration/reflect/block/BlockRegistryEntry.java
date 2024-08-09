package io.wispforest.owo.registration.reflect.block;

import io.wispforest.owo.registration.reflect.entry.MemoizedRegistryEntry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

import java.util.function.Supplier;

public final class BlockRegistryEntry<T extends Block> extends MemoizedRegistryEntry<T, Block> implements ItemConvertible {
    public BlockRegistryEntry(Supplier<T> factory) {
        super(factory);
    }

    @Override
    public Item asItem() {
        return this.value().asItem();
    }
}