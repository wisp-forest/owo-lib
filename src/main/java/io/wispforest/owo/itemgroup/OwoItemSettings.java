package io.wispforest.owo.itemgroup;

import io.wispforest.owo.Owo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @deprecated Replaced with {@link OwoItemSettingsExtension}.
 */
@Deprecated(forRemoval = true)
public class OwoItemSettings extends Item.Settings {
    public OwoItemSettings group(ItemGroupReference ref) {
        return (OwoItemSettings) super.groupSup(ref);
    }

    /**
     * @param group The item group this item should appear in
     */
    @Deprecated
    public OwoItemSettings group(OwoItemGroup group) {
        return (OwoItemSettings) super.group(group);
    }

    @Deprecated
    public OwoItemGroup group() {
        return this.groupSupplier().get();
    }

    public OwoItemSettings group(Supplier<OwoItemGroup> groupSupplier) {
        return (OwoItemSettings) super.group(groupSupplier);
    }

    public Supplier<OwoItemGroup> groupSupplier() {
        return this.groupSupplier();
    }

    public OwoItemSettings tab(int tab) {
        return (OwoItemSettings) super.tab(tab);
    }

    public int tab() {
        return super.tab();
    }

    /**
     * @param generator The function this item uses for creating stacks in the
     *                  {@link OwoItemGroup} it is in, by default this will be {@link OwoItemGroup#DEFAULT_STACK_GENERATOR}
     */
    public OwoItemSettings stackGenerator(BiConsumer<Item, ItemGroup.Entries> generator) {
        return (OwoItemSettings) super.stackGenerator(generator);
    }

    public BiConsumer<Item, ItemGroup.Entries> stackGenerator() {
        return super.stackGenerator();
    }

    /**
     * Automatically increment {@link net.minecraft.stat.Stats#USED}
     * for this item every time {@link Item#use(World, PlayerEntity, Hand)}
     * returns an accepted result
     */
    public OwoItemSettings trackUsageStat() {
        return (OwoItemSettings) super.trackUsageStat();
    }

    public boolean shouldTrackUsageStat() {
        return super.shouldTrackUsageStat();
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