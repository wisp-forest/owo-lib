package io.wispforest.owo.util.pond;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.BiConsumer;

/**
 * Used for storing a tab index for use with a {@link OwoItemGroup}
 * inside the vanilla {@link Item.Settings}
 */
public interface OwoItemSettingsExtensions {

    /**
     * @return The index of the tab the target item should reside in
     */
    int owo$tab();

    /**
     * Sets the tab index the target item should reside in
     *
     * @param tab The 0-indexed tab id
     */
    void owo$setTab(int tab);

    BiConsumer<Item, DefaultedList<ItemStack>> owo$stackGenerator();

    void owo$setStackGenerator(BiConsumer<Item, DefaultedList<ItemStack>> appender);

}
