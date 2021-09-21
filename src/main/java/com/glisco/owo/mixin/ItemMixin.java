package com.glisco.owo.mixin;

import com.glisco.owo.itemgroup.OwoItemExtensions;
import com.glisco.owo.itemgroup.OwoItemSettings;
import com.glisco.owo.itemgroup.OwoItemGroup;
import com.glisco.owo.itemgroup.gui.ItemGroupTab;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin implements OwoItemExtensions {

    @Final
    @Shadow
    @Mutable
    protected ItemGroup group;
    private ItemGroupTab tab = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void grabTab(Item.Settings settings, CallbackInfo ci) {
        if (settings instanceof OwoItemSettings owoSettings && group instanceof OwoItemGroup group) this.tab = group.getTab(owoSettings.getTab());
    }

    @Override
    public ItemGroupTab getTab() {
        return tab;
    }

    @Override
    public void setItemGroup(ItemGroup group) {
        this.group = group;
    }
}
