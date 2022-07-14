package io.wispforest.owo.mixin.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.wispforest.owo.util.RecipeSpecificRemainderStorage;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Inject(method = "deserialize", at = @At(value = "RETURN"))
    private static void deserializeRecipeSpecificRemainders(Identifier id, JsonObject json, CallbackInfoReturnable<Recipe<?>> cir){
        if(json.has("owo:remainders")){
            JsonObject remainders = json.getAsJsonObject("owo:remainders");

            for(Map.Entry<String, JsonElement> entry : remainders.entrySet()){
                Identifier item_id = Identifier.tryParse(entry.getKey());

                if(item_id == null) {
                    throw new JsonSyntaxException("OwO Remainders: A recipe Remainder has invalid item Identifier");
                }

                Optional<Item> item = Registry.ITEM.getOrEmpty(item_id);

                if(item.isEmpty()) {
                    throw new JsonSyntaxException("OwO Remainders: The given Item could not be found within the Item Registry");
                }

                if (entry.getValue() instanceof JsonObject jsonObject) {
                    if (jsonObject.has("item")) {
                        Identifier remainder_item_id = Identifier.tryParse(JsonHelper.getString(jsonObject, "item"));

                        if (remainder_item_id == null) {
                            throw new JsonSyntaxException("OwO Remainders: A recipe Remainder has invalid reminder item Identifier");
                        }

                        Optional<Item> remainderItem = Registry.ITEM.getOrEmpty(item_id);

                        if(remainderItem.isEmpty()) {
                            throw new JsonSyntaxException("OwO Remainders: The given Remainder Item could not be found within the Item Registry");
                        }

                        RecipeSpecificRemainderStorage.registerRecipeSpecificRemainder(id, item.get(), remainderItem.get());

                    } else {
                        throw new JsonSyntaxException("OwO Remainders: It seems that the recipe has defined a remainder but without specified remainder item to give back");
                    }
                } else {
                    throw new JsonSyntaxException("OwO Remainders: " + entry.getValue() + " expected to be a JsonObject which it was not");
                }
            }
        }
    }
}
