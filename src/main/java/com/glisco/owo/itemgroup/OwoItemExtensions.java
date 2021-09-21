package com.glisco.owo.itemgroup;

import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public interface OwoItemExtensions {

    ItemGroupTab getTab();

    void setItemGroup(ItemGroup group);

}
