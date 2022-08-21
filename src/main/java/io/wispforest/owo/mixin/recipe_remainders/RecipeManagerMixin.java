package io.wispforest.owo.mixin.recipe_remainders;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.wispforest.owo.util.RecipeRemainderStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Inject(method = "deserialize", at = @At(value = "RETURN"))
    private static void deserializeRecipeSpecificRemainders(Identifier id, JsonObject json, CallbackInfoReturnable<Recipe<?>> cir) {
        if (!json.has("owo:remainders")) return;

        var remainders = new HashMap<Item, ItemStack>();
        for (var remainderEntry : json.getAsJsonObject("owo:remainders").entrySet()) {
            var item = JsonHelper.asItem(new JsonPrimitive(remainderEntry.getKey()), remainderEntry.getKey());

            if (remainderEntry.getValue().isJsonObject()) {
                var remainderStack = ShapedRecipe.outputFromJson(remainderEntry.getValue().getAsJsonObject());
                remainders.put(item, remainderStack);
            } else {
                var remainderItem = JsonHelper.asItem(remainderEntry.getValue(), "item");
                remainders.put(item, new ItemStack(remainderItem));
            }
        }

        if (remainders.isEmpty()) return;
        RecipeRemainderStorage.store(id, remainders);
    }
}
