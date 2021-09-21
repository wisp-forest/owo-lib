package com.glisco.owo.itemgroup;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;

public class OwoItemSettings extends Item.Settings {

    private int tab = 0;

    public OwoItemSettings tab(int tab) {
        this.tab = tab;
        return this;
    }

    public int getTab() {
        return tab;
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
