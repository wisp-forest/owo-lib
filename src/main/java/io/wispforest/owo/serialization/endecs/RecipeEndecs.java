package io.wispforest.owo.serialization.endecs;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.ReflectionEndecBuilder;
import io.wispforest.owo.serialization.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.impl.StructField;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;

public class RecipeEndecs {
    private static final Endec<Item> CRAFTING_RESULT_ITEM = Endec.ofRegistry(Registries.ITEM)
            .validate(item -> {
                if(item == Items.AIR) throw new IllegalStateException("Crafting result must not be minecraft:air");

                return item;
            });

    public static final Endec<ItemStack> CRAFTING_RESULT = StructEndecBuilder.of(
            CRAFTING_RESULT_ITEM.field("item", ItemStack::getItem),
            StructField.defaulted("count", ExtraEndecs.POSITIVE_INT, ItemStack::getCount, 1),
            ItemStack::new
    );

    static final Endec<ItemStack> INGREDIENT = Endec.ofRegistry(Registries.ITEM)
            .validate(item -> {
                if(item == Items.AIR) throw new IllegalStateException("Empty ingredient not allowed here");

                return item;
            })
            .then(ItemStack::new, ItemStack::getItem);

    public static final Endec<CraftingRecipeCategory> CATEGORY_ENDEC = ReflectionEndecBuilder.createEnumSerializer(CraftingRecipeCategory.class);

}
