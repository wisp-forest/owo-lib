package io.wispforest.owo.registration.reflect.block;

import io.wispforest.owo.registration.reflect.entry.MemoizedRegistryEntry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.RegistryKey;

import java.util.function.Function;
import java.util.function.Supplier;

public final class BlockRegistryEntry<T extends Block> extends MemoizedRegistryEntry<T, Block> implements ItemConvertible {

    private final AbstractBlock.Settings settings;

    public BlockRegistryEntry(Function<AbstractBlock.Settings, T> factory, AbstractBlock.Settings settings) {
        super(() -> factory.apply(settings));

        this.settings = settings;
    }

    @Override
    public void setup(RegistryKey<Block> registryKey) {
        this.settings.registryKey(registryKey);
    }

    @Override
    public Item asItem() {
        return this.value().asItem();
    }
}