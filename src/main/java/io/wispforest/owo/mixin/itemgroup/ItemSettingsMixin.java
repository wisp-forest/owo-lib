package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.ItemGroupReference;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mixin(Item.Settings.class)
public class ItemSettingsMixin implements OwoItemSettingsExtension {
    private Supplier<OwoItemGroup> owo$group = null;
    private int owo$tab = 0;
    private BiConsumer<Item, ItemGroup.Entries> owo$stackGenerator = null;
    private boolean owo$trackUsageStat = false;

    @Override
    public Item.Settings group(ItemGroupReference ref) {
        this.owo$group = ref.groupSup();
        this.owo$tab = ref.tab();

        return (Item.Settings)(Object) this;
    }

    @Override
    public Item.Settings group(OwoItemGroup group) {
        this.owo$group = () -> group;

        return (Item.Settings)(Object) this;
    }

    @Override
    public Item.Settings group(Supplier<OwoItemGroup> groupSupplier) {
        this.owo$group = groupSupplier;

        return (Item.Settings)(Object) this;
    }

    @Override
    public OwoItemGroup group() {
        return owo$group.get();
    }

    @Override
    public Supplier<OwoItemGroup> groupSupplier() {
        return owo$group;
    }

    @Override
    public Item.Settings tab(int tab) {
        this.owo$tab = tab;

        return (Item.Settings)(Object) this;
    }

    @Override
    public int tab() {
        return owo$tab;
    }

    @Override
    public Item.Settings stackGenerator(BiConsumer<Item, ItemGroup.Entries> generator) {
        this.owo$stackGenerator = generator;

        return (Item.Settings)(Object) this;
    }

    @Override
    public BiConsumer<Item, ItemGroup.Entries> stackGenerator() {
        return owo$stackGenerator;
    }

    @Override
    public Item.Settings trackUsageStat() {
        this.owo$trackUsageStat = true;

        return (Item.Settings)(Object) this;
    }

    @Override
    public boolean shouldTrackUsageStat() {
        return owo$trackUsageStat;
    }
}
