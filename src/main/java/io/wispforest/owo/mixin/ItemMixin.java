package io.wispforest.owo.mixin;

import io.wispforest.owo.itemgroup.OwoItemExtensions;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtensions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin implements OwoItemExtensions {

    @Final
    @Shadow
    @Mutable
    protected ItemGroup group;

    @Unique
    private int tab = -1;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void grabTab(Item.Settings settings, CallbackInfo ci) {
        if (group instanceof OwoItemGroup group) this.tab = ((OwoItemSettingsExtensions) settings).getTabIndex();
    }

    @Override
    public int getTab() {
        return tab;
    }

    @Override
    public void setItemGroup(ItemGroup group) {
        this.group = group;
    }
}
