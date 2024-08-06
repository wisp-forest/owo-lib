package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.ItemGroupReference;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.BiConsumer;

@Mixin(Item.Properties.class)
public class ItemPropertiesMixin implements OwoItemSettingsExtension {
    private OwoItemGroup owo$group = null;
    private int owo$tab = 0;
    private BiConsumer<Item, CreativeModeTab.Output> owo$stackGenerator = null;
    private boolean owo$trackUsageStat = false;

    @Override
    public Item.Properties group(ItemGroupReference ref) {
        this.owo$group = ref.group();
        this.owo$tab = ref.tab();

        return (Item.Properties)(Object) this;
    }

    @Override
    public Item.Properties group(OwoItemGroup group) {
        this.owo$group = group;

        return (Item.Properties)(Object) this;
    }

    @Override
    public OwoItemGroup group() {
        return owo$group;
    }

    @Override
    public Item.Properties tab(int tab) {
        this.owo$tab = tab;

        return (Item.Properties)(Object) this;
    }

    @Override
    public int tab() {
        return owo$tab;
    }

    @Override
    public Item.Properties stackGenerator(BiConsumer<Item, CreativeModeTab.Output> generator) {
        this.owo$stackGenerator = generator;

        return (Item.Properties)(Object) this;
    }

    @Override
    public BiConsumer<Item, CreativeModeTab.Output> stackGenerator() {
        return owo$stackGenerator;
    }

    @Override
    public Item.Properties trackUsageStat() {
        this.owo$trackUsageStat = true;

        return (Item.Properties)(Object) this;
    }

    @Override
    public boolean shouldTrackUsageStat() {
        return owo$trackUsageStat;
    }
}
