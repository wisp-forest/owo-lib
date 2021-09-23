package com.glisco.owo.mixin;

import com.glisco.owo.itemgroup.OwoItemSettingsExtensions;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.Settings.class)
public class ItemSettingsMixin implements OwoItemSettingsExtensions {

    @Unique
    private int tab = 0;

    @Override
    public int getTabIndex() {
        return tab;
    }

    @Override
    public Item.Settings setTab(int tab) {
        this.tab = tab;
        return (Item.Settings) (Object) this;
    }
}
