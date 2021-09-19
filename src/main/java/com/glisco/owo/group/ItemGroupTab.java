package com.glisco.owo.group;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public record ItemGroupTab(ItemStack icon, String name, Tag<Item> itemTag, Identifier texture) {

    public boolean matches(Item item) {
        return itemTag == null || itemTag.contains(item);
    }

    public boolean matches(ItemStack stack) {
        return matches(stack.getItem());
    }

    public String getTranslationKey() {
        return "itemGroup.subTab." + name;
    }
}
