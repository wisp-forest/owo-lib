package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.util.pond.OwoItemSettingsExtensions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.BiConsumer;

@Mixin(Item.Settings.class)
public class ItemSettingsMixin implements OwoItemSettingsExtensions {

    @Unique
    private int owo$tab = 0;

    @Unique
    private BiConsumer<Item, DefaultedList<ItemStack>> owo$stackGenerator = OwoItemGroup.DEFAULT_STACK_GENERATOR;

    @Override
    public int owo$tab() {
        return owo$tab;
    }

    @Override
    public void owo$setTab(int tab) {
        this.owo$tab = tab;
    }

    @Override
    public BiConsumer<Item, DefaultedList<ItemStack>> owo$stackGenerator() {
        return this.owo$stackGenerator;
    }

    @Override
    public void owo$setStackGenerator(BiConsumer<Item, DefaultedList<ItemStack>> appender) {
        this.owo$stackGenerator = appender;
    }
}
