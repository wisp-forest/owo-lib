package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.itemgroup.base.OwoItemGroupEntries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.tag.TagKey;

@FunctionalInterface
public interface ContentSupplier {
    ContentSupplier EMPTY = (context, entries) -> {};

    static ContentSupplier fromTag(TagKey<Item> tag) {
        return (context, entries) -> entries.addAll(tag);
    }

    void addItems(ItemGroup.DisplayContext context, OwoItemGroupEntries entries);
}
