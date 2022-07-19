package io.wispforest.owo.mixin.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.wispforest.owo.util.RecipeSpecificRemainders;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Inject(method = "deserialize", at = @At(value = "RETURN"))
    private static void deserializeRecipeSpecificRemainders(Identifier id, JsonObject json, CallbackInfoReturnable<Recipe<?>> cir){
        if(json.has("owo:remainders")){
            JsonObject remainders = json.getAsJsonObject("owo:remainders");

            for(Map.Entry<String, JsonElement> entry : remainders.entrySet()){
                Item item = JsonHelper.asItem(new JsonPrimitive(entry.getKey()), entry.getKey());

                if (entry.getValue() instanceof JsonObject jsonObject) {
                    Item remainderItem = JsonHelper.getItem(jsonObject, "item");

                    RecipeSpecificRemainders.add(id, item, remainderItem);

                } else {
                    throw new JsonSyntaxException("owo Remainders: " + entry.getValue() + "was expected to be a JsonObject but found not to be");
                }
            }
        }
    }
}
