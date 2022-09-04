package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import io.wispforest.owo.util.pond.OwoItemSettingsExtensions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(Item.class)
public class ItemMixin implements OwoItemExtensions {

    @Final
    @Shadow
    @Mutable
    protected ItemGroup group;

    @Unique
    private int tab = -1;

    @Unique
    private BiConsumer<Item, DefaultedList<ItemStack>> stackGenerator = OwoItemGroup.DEFAULT_STACK_GENERATOR;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void grabTab(Item.Settings settings, CallbackInfo ci) {
        if (group instanceof OwoItemGroup) {
            this.tab = ((OwoItemSettingsExtensions) settings).owo$tab();
            this.stackGenerator = ((OwoItemSettingsExtensions) settings).owo$stackGenerator();
        }
    }

    @Override
    public int owo$tab() {
        return tab;
    }

    @Override
    public BiConsumer<Item, DefaultedList<ItemStack>> owo$stackGenerator() {
        return stackGenerator;
    }

    @Override
    public void owo$setItemGroup(ItemGroup group) {
        this.group = group;
    }
}
