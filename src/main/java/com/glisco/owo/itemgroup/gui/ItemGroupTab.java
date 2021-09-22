package com.glisco.owo.itemgroup.gui;

import com.glisco.owo.itemgroup.Icon;
import com.glisco.owo.itemgroup.OwoItemGroup;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * Represents a tab inside an {@link OwoItemGroup} that contains all items in the
 * passed {@code contentTag}. If you want to use {@link com.glisco.owo.itemgroup.OwoItemSettings#tab(int)} to
 * define the contents, use {@link #EMPTY} as the tag
 */
public record ItemGroupTab(Icon icon, String name, Tag<Item> contentTag, Identifier texture) implements OwoItemGroup.ButtonDefinition {

    public static final Tag<Item> EMPTY = Tag.of(Set.of());

    public static final Identifier DEFAULT_TEXTURE = new Identifier("owo", "textures/gui/tabs.png");

    public boolean includes(Item item) {
        return (contentTag != null && contentTag.contains(item));
    }

    public String getTranslationKey(String groupKey) {
        return "itemGroup." + groupKey + ".tab." + name;
    }
}
