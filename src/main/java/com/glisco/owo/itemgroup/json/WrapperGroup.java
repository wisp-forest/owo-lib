package com.glisco.owo.itemgroup.json;

import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import com.glisco.owo.itemgroup.TabbedItemGroup;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class WrapperGroup extends TabbedItemGroup {

    private final Supplier<ItemStack> icon;

    public WrapperGroup(int index, String name, List<ItemGroupTab> tabs, Supplier<ItemStack> icon) {
        super(index, name);
        this.tabs.clear();
        this.tabs.addAll(tabs);
        this.icon = icon;
    }

    @Override
    protected void setup() {}

    @Override
    public ItemStack createIcon() {
        return this.icon.get();
    }
}
