package io.wispforest.owo.serialization.endecs;

import com.mojang.datafixers.util.Either;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.StructEndecBuilder;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.collection.DefaultedList;

import java.util.Arrays;
import java.util.List;

public class IngredientEndec {

    private static final Endec<Ingredient.StackEntry> STACK_ENTRY_ENDEC = StructEndecBuilder.of(
            RecipeEndecs.INGREDIENT.field("item", e -> e.stack),
            Ingredient.StackEntry::new
    );

    private static final Endec<Ingredient.TagEntry> TAG_ENTRY_ENDEC = StructEndecBuilder.of(
            Endec.unprefixedTagKey(RegistryKeys.ITEM).field("tag", e -> e.tag),
            Ingredient.TagEntry::new
    );

    private static final Endec<Ingredient.Entry> INGREDIENT_ENTRY_ENDEC = new XorEndec<>(STACK_ENTRY_ENDEC, TAG_ENTRY_ENDEC)
            .then(
                    either -> either.map(stackEntry -> stackEntry, tagEntry -> tagEntry),
                    entry -> {
                        if (entry instanceof Ingredient.TagEntry tagEntry) return Either.right(tagEntry);
                        if (entry instanceof Ingredient.StackEntry stackEntry) return Either.left(stackEntry);

                        throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
                    }
            );

    public static final Endec<Ingredient> ALLOW_EMPTY_CODEC = createEndec(true);
    public static final Endec<Ingredient> DISALLOW_EMPTY_CODEC = createEndec(false);

//    public static final Endec<Ingredient> RAW_DATA = Endec.ITEM_STACK.list()
//            .then(
//                stackList -> Ingredient.ofEntries(stackList.stream().map(Ingredient.StackEntry::new)),
//                ingredient -> Arrays.stream(ingredient.getMatchingStacks()).toList()
//            );

    private static Endec<Ingredient> createEndec(boolean allowEmpty) {
        Endec<Ingredient.Entry[]> endec = INGREDIENT_ENTRY_ENDEC.list()
                .validate(entries -> {
                    if(!allowEmpty && entries.size() < 1){
                        throw new IllegalStateException("Item array cannot be empty, at least one item must be defined");
                    }

                    return entries;
                })
                .then(entries -> entries.toArray(new Ingredient.Entry[0]), List::of);

        return new EitherEndec<>(endec, INGREDIENT_ENTRY_ENDEC)
                .then(
                    either -> either.map(Ingredient::new, entry -> new Ingredient(new Ingredient.Entry[]{entry})),
                    ingredient -> {
                        if(ingredient.entries.length == 0 && !allowEmpty){
                            throw new IllegalStateException("Item array cannot be empty, at least one item must be defined");
                        }

                        return (ingredient.entries.length == 1)
                                ? Either.right(ingredient.entries[0])
                                : Either.left(ingredient.entries);
                    }
                );
    }
}
