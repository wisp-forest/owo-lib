package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtensions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.BiConsumer;

@Mixin(Item.Settings.class)
public class ItemSettingsMixin implements OwoItemSettingsExtensions {

    @Unique
    private int tab = 0;

    @Unique
    private BiConsumer<Item, DefaultedList<ItemStack>> stackGenerator = OwoItemGroup.DEFAULT_STACK_GENERATOR;

    @Override
    public int getTabIndex() {
        return tab;
    }

    @Override
    public Item.Settings setTab(int tab) {
        this.tab = tab;
        return (Item.Settings) (Object) this;
    }

    @Override
    public BiConsumer<Item, DefaultedList<ItemStack>> getStackGenerator() {
        return this.stackGenerator;
    }

    @Override
    public void setStackGenerator(BiConsumer<Item, DefaultedList<ItemStack>> appender) {
        this.stackGenerator = appender;
    }
}
