package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.base.OwoItemGroupEntries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.tag.TagKey;

///
/// Supplies entries for [OwoItemGroup] tab similar to [ItemGroup.EntryCollector] but uses
/// [OwoItemGroupEntries] instead of [ItemGroup.Entries]
///
@FunctionalInterface
public interface OwoEntryCollector {
    OwoEntryCollector EMPTY = (context, entries) -> {};

    static OwoEntryCollector fromTag(TagKey<Item> tag) {
        return (context, entries) -> entries.addAll(tag);
    }

    void addItems(ItemGroup.DisplayContext context, OwoItemGroupEntries entries);
}
