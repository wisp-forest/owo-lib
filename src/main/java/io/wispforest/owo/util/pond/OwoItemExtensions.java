package io.wispforest.owo.util.pond;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.json.GroupTabLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.Nullable;

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
    BiConsumer<Item, ItemGroup.Entries> owo$stackGenerator();

    /**
     * Sets the group of this item, used by {@link GroupTabLoader} to ensure
     * all {@code ItemGroup} references in items are correct for data-driven owo groups
     *
     * @param group The group to replace the current on with
     */
    void owo$setGroup(ItemGroup group);

    /**
     * @return The item group this item should reside in
     */
    @Nullable ItemGroup owo$group();

}
