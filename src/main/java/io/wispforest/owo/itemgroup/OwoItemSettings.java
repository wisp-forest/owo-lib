package io.wispforest.owo.itemgroup;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class OwoItemSettings extends Item.Settings {

    @Nullable
    private Supplier<OwoItemGroup> group = () -> null;
    private int tab = 0;
    private BiConsumer<Item, ItemGroup.Entries> stackGenerator = OwoItemGroup.DEFAULT_STACK_GENERATOR;
    private boolean trackUsageStat = false;

    public OwoItemSettings group(ItemGroupReference ref) {
        this.group = ref.groupSup();
        this.tab = ref.tab();
        return this;
    }

    /**
     * @param group The item group this item should appear in
     */
    @Deprecated
    public OwoItemSettings group(OwoItemGroup group) {
        this.group = () -> group;
        return this;
    }

    @Deprecated
    public OwoItemGroup group() {
        return this.group.get();
    }

    public OwoItemSettings group(Supplier<OwoItemGroup> groupSupplier) {
        this.group = groupSupplier;
        return this;
    }

    public Supplier<OwoItemGroup> groupSupplier() {
        return this.group;
    }

    public OwoItemSettings tab(int tab) {
        this.tab = tab;
        return this;
    }

    public int tab() {
        return this.tab;
    }

    /**
     * @param generator The function this item uses for creating stacks in the
     *                  {@link OwoItemGroup} it is in, by default this will be {@link OwoItemGroup#DEFAULT_STACK_GENERATOR}
     */
    public OwoItemSettings stackGenerator(BiConsumer<Item, ItemGroup.Entries> generator) {
        this.stackGenerator = generator;
        return this;
    }

    public BiConsumer<Item, ItemGroup.Entries> stackGenerator() {
        return this.stackGenerator;
    }

    /**
     * Automatically increment {@link net.minecraft.stat.Stats#USED}
     * for this item every time {@link Item#use(World, PlayerEntity, Hand)}
     * returns an accepted result
     */
    public OwoItemSettings trackUsageStat() {
        this.trackUsageStat = true;
        return this;
    }

    public boolean shouldTrackUsageStat() {
        return this.trackUsageStat;
    }

    @Override
    public OwoItemSettings maxCount(int maxCount) {
        return (OwoItemSettings) super.maxCount(maxCount);
    }

    @Override
    public OwoItemSettings maxDamage(int maxDamage) {
        return (OwoItemSettings) super.maxDamage(maxDamage);
    }

    @Override
    public OwoItemSettings recipeRemainder(Item recipeRemainder) {
        return (OwoItemSettings) super.recipeRemainder(recipeRemainder);
    }

    @Override
    public OwoItemSettings rarity(Rarity rarity) {
        return (OwoItemSettings) super.rarity(rarity);
    }

    @Override
    public OwoItemSettings fireproof() {
        return (OwoItemSettings) super.fireproof();
    }
}
