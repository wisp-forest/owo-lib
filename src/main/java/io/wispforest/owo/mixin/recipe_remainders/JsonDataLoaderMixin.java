package io.wispforest.owo.mixin.recipe_remainders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.util.RecipeRemainderStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.Reader;
import java.util.HashMap;

@Mixin(JsonDataLoader.class)
public abstract class JsonDataLoaderMixin {

    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonParser;parseReader(Ljava/io/Reader;)Lcom/google/gson/JsonElement;"))
    private static JsonElement deserializeRecipeSpecificRemainders(Reader jsonReader, Operation<JsonElement> original, @Local(argsOnly = true) String dataType, @Local(ordinal = 1) Identifier recipeId) {
        var element = original.call(jsonReader);

        if (dataType.equals(RegistryKeys.getPath(RegistryKeys.RECIPE)) && element instanceof JsonObject json) {
            if (json.has("owo:remainders")) {
                var remainders = new HashMap<Item, ItemStack>();

                for (var remainderEntry : json.getAsJsonObject("owo:remainders").entrySet()) {
                    var item = JsonHelper.asItem(new JsonPrimitive(remainderEntry.getKey()), remainderEntry.getKey());

                    if (remainderEntry.getValue().isJsonObject()) {
                        var remainderStack = ItemStack.CODEC.parse(JsonOps.INSTANCE, remainderEntry.getValue().getAsJsonObject()).getOrThrow(JsonParseException::new);
                        remainders.put(item.value(), remainderStack);
                    } else {
                        var remainderItem = JsonHelper.asItem(remainderEntry.getValue(), "item");
                        remainders.put(item.value(), new ItemStack(remainderItem));
                    }
                }

                if (!remainders.isEmpty()) RecipeRemainderStorage.store(recipeId, remainders);
            }
        }

        return element;
    }
}
