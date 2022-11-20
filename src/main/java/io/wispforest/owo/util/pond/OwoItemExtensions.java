package io.wispforest.owo.util.pond;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.BiConsumer;

public interface OwoItemExtensions {

    /**
     * @return The 0-indexed tab id this item resides in, {@code -1} if none is defined
     */
    int owo$tab();

    /**
     * @return The function used for adding stacks of
     * this item to an {@link OwoItemGroup} it resides in
     */
    BiConsumer<Item, DefaultedList<ItemStack>> owo$stackGenerator();

    /**
     * Sets the group of this item, used by {@link OwoItemGroupLoader} to ensure
     * all {@code ItemGroup} references in items are correct for data-driven owo groups
     *
     * @param group The group to replace the current on with
     */
    void owo$setGroup(ItemGroup group);

}
