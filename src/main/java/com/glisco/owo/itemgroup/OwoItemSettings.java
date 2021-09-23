package com.glisco.owo.itemgroup;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;

/**
 * A wrapper for {@link ItemGroup} that provides easy access to the methods implemented onto
 * it from {@link OwoItemSettingsExtensions} for defining the tab of item in a tabbed group
 */
public class OwoItemSettings extends Item.Settings {

    public OwoItemSettings tab(int tab) {
        ((OwoItemSettingsExtensions) this).setTab(tab);
        return this;
    }

    public int getTab() {
        return ((OwoItemSettingsExtensions) this).getTabIndex();
    }

    public OwoItemSettings food(FoodComponent foodComponent) {
        return (OwoItemSettings) super.food(foodComponent);
    }

    public OwoItemSettings maxCount(int maxCount) {
        return (OwoItemSettings) super.maxCount(maxCount);
    }

    public OwoItemSettings maxDamageIfAbsent(int maxDamage) {
        return (OwoItemSettings) super.maxDamageIfAbsent(maxDamage);
    }

    public OwoItemSettings maxDamage(int maxDamage) {
        return (OwoItemSettings) super.maxDamage(maxDamage);
    }

    public OwoItemSettings recipeRemainder(Item recipeRemainder) {
        return (OwoItemSettings) super.recipeRemainder(recipeRemainder);
    }

    public OwoItemSettings group(ItemGroup group) {
        return (OwoItemSettings) super.group(group);
    }

    public OwoItemSettings rarity(Rarity rarity) {
        return (OwoItemSettings) super.rarity(rarity);
    }

    public OwoItemSettings fireproof() {
        return (OwoItemSettings) super.fireproof();
    }

}
