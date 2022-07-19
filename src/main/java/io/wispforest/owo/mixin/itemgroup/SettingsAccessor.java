package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Settings.class)
public interface SettingsAccessor {

    @Accessor("maxCount")
    int owo$getMaxCount();

    @Accessor("maxDamage")
    int owo$getMaxDamage();

    @Accessor("recipeRemainder")
    Item owo$getRecipeRemainder();

    @Accessor("group")
    ItemGroup owo$getGroup();

    @Accessor("rarity")
    Rarity owo$getRarity();

    @Accessor("foodComponent")
    FoodComponent owo$getFoodComponent();

    @Accessor("fireproof")
    boolean owo$isFireproof();
}
