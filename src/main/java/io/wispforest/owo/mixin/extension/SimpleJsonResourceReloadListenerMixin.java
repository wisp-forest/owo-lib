package io.wispforest.owo.mixin.extension;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.extension.recipe.RecipeManagerAccessor;
import io.wispforest.owo.util.RecipeRemainderStorage;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.Reader;
import java.util.HashMap;

@Mixin(SimpleJsonResourceReloadListener.class)
public abstract class SimpleJsonResourceReloadListenerMixin {

    @WrapOperation(
        method = "scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/StrictJsonParser;parse(Ljava/io/Reader;)Lcom/google/gson/JsonElement;"
        )
    )
    private static JsonElement loadRecipeExtensions(
        Reader jsonReader,
        Operation<JsonElement> original,
        @Local(argsOnly = true) FileToIdConverter finder,
        @Local(ordinal = 1) Identifier recipeId
    ) {
        var element = original.call(jsonReader);

        if (RecipeManagerAccessor.owo$getFinder() == finder && element instanceof JsonObject json) {
            if (json.has(Owo.id("remainders").toString())) {
                var remainders = new HashMap<Item, ItemStack>();

                for (var remainderEntry : json.getAsJsonObject(Owo.id("remainders").toString()).entrySet()) {
                    var item = GsonHelper.convertToItem(new JsonPrimitive(remainderEntry.getKey()), remainderEntry.getKey());

                    if (remainderEntry.getValue().isJsonObject()) {
                        var remainderStack = ItemStack.CODEC.parse(
                            JsonOps.INSTANCE,
                            remainderEntry.getValue().getAsJsonObject()
                        ).getOrThrow(JsonParseException::new);
                        remainders.put(item.value(), remainderStack);
                    } else {
                        var remainderItem = GsonHelper.convertToItem(remainderEntry.getValue(), "item");
                        remainders.put(item.value(), new ItemStack(remainderItem));
                    }
                }

                if (!remainders.isEmpty()) RecipeRemainderStorage.store(recipeId, remainders);
            }
        }

        return element;
    }
}
