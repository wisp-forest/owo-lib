package io.wispforest.owo.itemgroup;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class OwoItemSettings extends Item.Settings {

    @Nullable
    private OwoItemGroup group = null;
    private int tab = 0;
    private BiConsumer<Item, ItemGroup.Entries> stackGenerator = OwoItemGroup.DEFAULT_STACK_GENERATOR;
    private boolean trackUsageStat = false;

    public OwoItemSettings group(ItemGroupReference ref) {
        this.group = ref.group();
        this.tab = ref.tab();
        return this;
    }

    /**
     * @param group The item group this item should appear in
     */
    public OwoItemSettings group(OwoItemGroup group) {
        this.group = group;
        return this;
    }

    public OwoItemGroup group() {
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
    public OwoItemSettings equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
        return (OwoItemSettings) super.equipmentSlot(equipmentSlotProvider);
    }

    @Override
    public OwoItemSettings customDamage(CustomDamageHandler handler) {
        return (OwoItemSettings) super.customDamage(handler);
    }

    @Override
    public OwoItemSettings food(FoodComponent foodComponent) {
        return (OwoItemSettings) super.food(foodComponent);
    }

    @Override
    public OwoItemSettings maxCount(int maxCount) {
        return (OwoItemSettings) super.maxCount(maxCount);
    }

    @Override
    public OwoItemSettings maxDamageIfAbsent(int maxDamage) {
        return (OwoItemSettings) super.maxDamageIfAbsent(maxDamage);
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
