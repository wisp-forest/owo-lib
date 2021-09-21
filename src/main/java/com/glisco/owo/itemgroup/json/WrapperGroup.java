package com.glisco.owo.itemgroup.json;

import com.glisco.owo.itemgroup.OwoItemGroup;
import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Supplier;

/**
 * Used to replace a vanilla or modded item group to add the JSON-defined
 * tabs while keeping the same name, id and icon
 */
@ApiStatus.Internal
public class WrapperGroup extends OwoItemGroup {

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
