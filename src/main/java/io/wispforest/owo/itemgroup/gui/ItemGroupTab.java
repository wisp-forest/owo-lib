package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Represents a tab inside an {@link OwoItemGroup} that contains all items in the
 * passed {@code contentTag}. If you want to use {@link OwoItemSettings#tab(int)} to
 * define the contents, use {@code null} as the tag
 */
public record ItemGroupTab(Icon icon, String name, TagKey<Item> contentTag,
                           Identifier texture) implements OwoItemGroup.ButtonDefinition {

    public static final Identifier DEFAULT_TEXTURE = new Identifier("owo", "textures/gui/tabs.png");

    public boolean includes(Item item) {
        return this.contentTag != null && item.getRegistryEntry().isIn(contentTag);
    }

    public String getTranslationKey(String groupKey) {
        return groupKey + ".tab." + name;
    }
}
