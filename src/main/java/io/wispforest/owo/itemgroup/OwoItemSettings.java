package io.wispforest.owo.itemgroup;

import io.wispforest.owo.mixin.itemgroup.FabricItemInternalsAccessor;
import io.wispforest.owo.mixin.itemgroup.FabricItemInternalsAccessor.ExtraDataAccessor;
import io.wispforest.owo.mixin.itemgroup.SettingsAccessor;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A wrapper for {@link ItemGroup} that provides easy access to the methods implemented onto
 * it from {@link OwoItemSettingsExtensions} for defining the tab of item in a tabbed group
 * as well as the function used for actually adding stacks
 */
public class OwoItemSettings extends FabricItemSettings {

    public OwoItemSettings tab(int tab) {
        ((OwoItemSettingsExtensions) this).setTab(tab);
        return this;
    }

    public int getTab() {
        return ((OwoItemSettingsExtensions) this).getTabIndex();
    }

    /**
     * @param generator The function this item uses for creating stacks in the
     *                  {@link OwoItemGroup} it is in, by default this will be {@link OwoItemGroup#DEFAULT_STACK_GENERATOR}
     */
    public OwoItemSettings stackGenerator(BiConsumer<Item, DefaultedList<ItemStack>> generator) {
        ((OwoItemSettingsExtensions) this).setStackGenerator(generator);
        return this;
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
    public OwoItemSettings group(ItemGroup group) {
        return (OwoItemSettings) super.group(group);
    }

    @Override
    public OwoItemSettings rarity(Rarity rarity) {
        return (OwoItemSettings) super.rarity(rarity);
    }

    @Override
    public OwoItemSettings fireproof() {
        return (OwoItemSettings) super.fireproof();
    }

    /**
     * Method used to make a copy of a given {@link Item.Settings}
     *
     * @param settings The Settings to copy from
     * @return A 1:1 Deep copy of the given {@link Item.Settings} as {@link OwoItemSettings}
     */
    public static OwoItemSettings copyFrom(Item.Settings settings){
        OwoItemSettings settingsNew = new OwoItemSettings();

        if(settings instanceof OwoItemSettings oldOwoItemSettings){
            settingsNew.tab(oldOwoItemSettings.getTab());
        }

        if(settings instanceof FabricItemSettings oldFabricItemSettings){
            ExtraDataAccessor oldData = (ExtraDataAccessor) (Object) FabricItemInternalsAccessor.owo$getExtraData().get(oldFabricItemSettings);

            if (oldData != null) {
                ((FabricItemSettings)settingsNew).customDamage(oldData.owo$getCustomDamageHandler());
                ((FabricItemSettings)settingsNew).equipmentSlot(oldData.owo$getEquipmentSlotProvider());
            }
        }

        SettingsAccessor settingsAccessor = (SettingsAccessor) settings;

        if (settingsAccessor.owo$isFireproof()) {
            settingsNew.fireproof();
        }

        settingsNew.group(settingsAccessor.owo$getGroup())
                .food(settingsAccessor.owo$getFoodComponent())
                .recipeRemainder(settingsAccessor.owo$getRecipeRemainder())
                .maxCount(settingsAccessor.owo$getMaxCount())
                .maxDamageIfAbsent(settingsAccessor.owo$getMaxDamage())
                .rarity(settingsAccessor.owo$getRarity());

        return settingsNew;
    }

}
