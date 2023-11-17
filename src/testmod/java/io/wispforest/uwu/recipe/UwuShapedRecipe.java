package io.wispforest.uwu.recipe;

import com.google.common.collect.Sets;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endecs.IngredientEndec;
import io.wispforest.owo.serialization.endecs.RecipeEndecs;
import io.wispforest.owo.serialization.impl.AttributeEndecBuilder;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import io.wispforest.owo.serialization.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.impl.StructField;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UwuShapedRecipe extends ShapedRecipe {

    public static RecipeSerializer<UwuShapedRecipe> RECIPE_SERIALIZER;

    public UwuShapedRecipe(String group, CraftingRecipeCategory category, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack result, boolean showNotification) {
        super(group, category, width, height, ingredients, result, showNotification);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RECIPE_SERIALIZER;
    }

    public static void init(){
        RECIPE_SERIALIZER = Registry.register(
                Registries.RECIPE_SERIALIZER,
                new Identifier("uwu:crafting_shaped"),
                new EndecRecipeSerializer<>(ENDEC)
        );
    }

    //--

    private static final Endec<UwuShapedRecipe> FROM_RAW_RECIPE = RawShapedRecipe.ENDEC.xmap(recipe -> {
        String[] strings = ShapedRecipe.removePadding(recipe.pattern);
        int i = strings[0].length();
        int j = strings.length;
        DefaultedList<Ingredient> defaultedList = DefaultedList.ofSize(i * j, Ingredient.EMPTY);
        Set<String> set = Sets.newHashSet(recipe.key.keySet());

        for(int k = 0; k < strings.length; ++k) {
            String string = strings[k];

            for(int l = 0; l < string.length(); ++l) {
                String string2 = string.substring(l, l + 1);
                Ingredient ingredient = string2.equals(" ") ? Ingredient.EMPTY : (Ingredient)recipe.key.get(string2);

                if (ingredient == null) {
                    throw new IllegalStateException("Pattern references symbol '" + string2 + "' but it's not defined in the key");
                }

                set.remove(string2);
                defaultedList.set(l + i * k, ingredient);
            }
        }

        if (!set.isEmpty()) throw new IllegalStateException("Key defines symbols that aren't used in pattern: " + set);

        return new UwuShapedRecipe(recipe.group, recipe.category, i, j, defaultedList, recipe.result, recipe.showNotification);
    }, recipe -> {
        throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
    });

    private static final Endec<DefaultedList<Ingredient>> INGREDIENTS = IngredientEndec.ALLOW_EMPTY_CODEC.list()
            .conform(size -> DefaultedList.ofSize(size, Ingredient.EMPTY));

    private static final Endec<UwuShapedRecipe> FROM_INSTANCE = StructEndecBuilder.of(
            Endec.STRING.field("group", ShapedRecipe::getGroup),
            RecipeEndecs.CATEGORY_ENDEC.field("category", ShapedRecipe::getCategory),
            Endec.VAR_INT.field("width", ShapedRecipe::getWidth),
            Endec.VAR_INT.field("height", ShapedRecipe::getHeight),
            INGREDIENTS.field("ingredients", ShapedRecipe::getIngredients),
            RecipeEndecs.CRAFTING_RESULT.field("result", recipe -> recipe.getResult(null)),
            Endec.BOOLEAN.field("show_notification", ShapedRecipe::showNotification),
            UwuShapedRecipe::new
    );

    private static final Endec<UwuShapedRecipe> ENDEC = new AttributeEndecBuilder<>(FROM_RAW_RECIPE, SerializationAttribute.HUMAN_READABLE)
            .orElse(FROM_INSTANCE);

    //--



    private record RawShapedRecipe(String group, CraftingRecipeCategory category, Map<String, Ingredient> key, List<String> pattern, ItemStack result, boolean showNotification) {
        private static final Endec<List<String>> PATTERN_ENDEC = Endec.STRING.list().validate(rows -> {
            if (rows.size() > 3) throw new IllegalStateException("Invalid pattern: too many rows, 3 is maximum");
            if (rows.isEmpty()) throw new IllegalStateException("Invalid pattern: empty pattern not allowed");

            int i = rows.get(0).length();

            for(String string : rows) {
                if (string.length() > 3) throw new IllegalStateException("Invalid pattern: too many columns, 3 is maximum");
                if (i != string.length()) throw new IllegalStateException("Invalid pattern: each row must be the same width");
            }

            return rows;
        });

        public static final Endec<RawShapedRecipe> ENDEC = StructEndecBuilder.of(
                StructField.defaulted("group", Endec.STRING, recipe -> recipe.group, ""),
                StructField.defaulted("category", RecipeEndecs.CATEGORY_ENDEC, recipe -> recipe.category, CraftingRecipeCategory.MISC),
                IngredientEndec.DISALLOW_EMPTY_CODEC.map().keyValidator(UwuShapedRecipe::keyEntryValidator).field("key", recipe -> recipe.key),
                PATTERN_ENDEC.field("pattern", recipe -> recipe.pattern),
                RecipeEndecs.CRAFTING_RESULT.field("result", recipe -> recipe.result),
                StructField.defaulted("show_notification", Endec.BOOLEAN, recipe -> recipe.showNotification, true),
                RawShapedRecipe::new
        );
    }

    private static String keyEntryValidator(String key){
        if (key.length() != 1) {
            throw new IllegalStateException("Invalid key entry: '" + key + "' is an invalid symbol (must be 1 character only).");
        } else if(" ".equals(key)){
            throw new IllegalStateException("Invalid key entry: ' ' is a reserved symbol.");
        }

        return key;
    }
}