package io.wispforest.owo.itemgroup;

import io.wispforest.owo.itemgroup.json.GroupTabLoader;
import net.minecraft.item.ItemGroup;

public interface OwoItemExtensions {

    /**
     * @return The 0-indexed tab id this item resides in, {@code -1} if none is defined
     */
    int getTab();

    /**
     * Sets the group of this item, used by {@link GroupTabLoader} to ensure
     * all {@code ItemGroup} references in items are correct for data-driven owo groups
     *
     * @param group The group to replace the current on with
     */
    void setItemGroup(ItemGroup group);

}
