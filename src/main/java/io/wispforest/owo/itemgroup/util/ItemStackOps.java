package io.wispforest.owo.itemgroup.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;

public class ItemStackOps {

    ///
    /// Locates the given [TagKey] entries from its respecitve registry
    /// and attempt to either grab all the [Block]s found and convert
    /// them to an [Item] or just grab all items in which the [Item#getDefaultStack]
    /// method will be used to get a stack.
    ///
    /// @return All [ItemStack] entries from the given [ItemConvertible] tag
    ///
    public static SequencedCollection<ItemStack> getStacks(TagKey<? extends ItemConvertible> tagKey) {
        var id = tagKey.registryRef().getValue();

        SequencedCollection<ItemStack> stacks;

        if (Objects.equals(id.getPath(), "item")) {
            stacks = Registries.ITEM.getOptional((TagKey<Item>) tagKey)
                .map(entries -> {
                    return entries.stream()
                        .map(entry -> entry.value().getDefaultStack())
                        .toList();
                })
                .orElse(List.of());
        } else if (Objects.equals(id.getPath(), "block")) {
            stacks = Registries.BLOCK.getOptional((TagKey<Block>) tagKey)
                .map(entries -> {
                    return entries.stream()
                        .map(entry -> entry.value().asItem())
                        .filter(item -> item != Items.AIR)
                        .map(Item::getDefaultStack)
                        .toList();
                })
                .orElse(List.of());
        } else {
            throw new IllegalStateException("Unable to handle the given ItemConvertible based tag as its neither item nor block tag!");
        }

        return stacks;
    }

    public static SequencedCollection<ItemStack> toItemStacks(SequencedCollection<? extends ItemConvertible> items) {
        return items.stream()
            .map(item -> item.asItem().getDefaultStack())
            .filter(stack -> !stack.isEmpty())
            .toList();
    }
}
