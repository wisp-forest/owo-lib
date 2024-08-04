package io.wispforest.owo.itemgroup;

import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import java.util.function.BiConsumer;

/**
 * @deprecated Replaced with {@link OwoItemSettingsExtension}.
 */
@Deprecated(forRemoval = true)
public class OwoItemSettings extends Item.Properties {
    public OwoItemSettings group(ItemGroupReference ref) {
        return (OwoItemSettings) super.group(ref);
    }

    /**
     * @param group The item group this item should appear in
     */
    public OwoItemSettings group(OwoItemGroup group) {
        return (OwoItemSettings) super.group(group);
    }

    public OwoItemGroup group() {
        return super.group();
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
    public OwoItemSettings stackGenerator(BiConsumer<Item, CreativeModeTab.Output> generator) {
        return (OwoItemSettings) super.stackGenerator(generator);
    }

    public BiConsumer<Item, CreativeModeTab.Output> stackGenerator() {
        return super.stackGenerator();
    }

    /**
     * Automatically increment {@link net.minecraft.stats.Stats#ITEM_USED}
     * for this item every time {@link Item#use(Level, Player, InteractionHand)}
     * returns an accepted result
     */
    public OwoItemSettings trackUsageStat() {
        return (OwoItemSettings) super.trackUsageStat();
    }

    public boolean shouldTrackUsageStat() {
        return super.shouldTrackUsageStat();
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
    public OwoItemSettings stacksTo(int maxCount) {
        return (OwoItemSettings) super.stacksTo(maxCount);
    }

    @Override
    public OwoItemSettings durability(int maxDamage) {
        return (OwoItemSettings) super.durability(maxDamage);
    }

    @Override
    public OwoItemSettings craftRemainder(Item recipeRemainder) {
        return (OwoItemSettings) super.craftRemainder(recipeRemainder);
    }

    @Override
    public OwoItemSettings rarity(Rarity rarity) {
        return (OwoItemSettings) super.rarity(rarity);
    }

    @Override
    public OwoItemSettings fireResistant() {
        return (OwoItemSettings) super.fireResistant();
    }
}