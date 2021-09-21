package com.glisco.owo.mixin;

import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import com.glisco.owo.itemgroup.OwoItemExtensions;
import com.glisco.owo.itemgroup.OwoItemSettings;
import com.glisco.owo.itemgroup.TabbedItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin implements OwoItemExtensions {

    @Shadow @Final protected ItemGroup group;
    private ItemGroupTab tab = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void grabTab(Item.Settings settings, CallbackInfo ci) {
        if (settings instanceof OwoItemSettings owoSettings && group instanceof TabbedItemGroup group) this.tab = group.getTab(owoSettings.getTab());
    }

    @Override
    public ItemGroupTab getTab() {
        return tab;
    }
}
