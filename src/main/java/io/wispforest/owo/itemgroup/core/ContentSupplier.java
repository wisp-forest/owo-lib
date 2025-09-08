package io.wispforest.owo.itemgroup.core;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

@FunctionalInterface
public interface ContentSupplier {
    ContentSupplier EMPTY = (context, entries) -> {};

    static ContentSupplier fromTag(TagKey<Item> tag) {
        return (context, entries) -> {
            Registries.ITEM.streamEntries()
                .filter(entry -> entry.isIn(tag))
                .forEach(ref -> entries.add(ref.value()));
        };
    }

    void addItems(ItemGroup.DisplayContext context, ItemGroup.Entries entries);
}
