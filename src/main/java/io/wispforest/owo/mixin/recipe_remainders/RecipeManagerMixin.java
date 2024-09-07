package io.wispforest.owo.mixin.recipe_remainders;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.util.RecipeRemainderStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resources/io/ResourceManager;Lnet/minecraft/util/profiling/Profiler;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeHolder;<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/crafting/Recipe;)V"))
    private void deserializeRecipeSpecificRemainders(Map<Identifier, JsonElement> object, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci, @Local Map.Entry<Identifier, JsonElement> entry) {
        var json = entry.getValue().getAsJsonObject();
        if (!json.has("owo:remainders")) return;

        var remainders = new HashMap<Item, ItemStack>();
        for (var remainderEntry : json.getAsJsonObject("owo:remainders").entrySet()) {
            var item = GsonHelper.convertToItem(new JsonPrimitive(remainderEntry.getKey()), remainderEntry.getKey());

            if (remainderEntry.getValue().isJsonObject()) {
                var remainderStack = ItemStack.CODEC.parse(JsonOps.INSTANCE, remainderEntry.getValue().getAsJsonObject()).getOrThrow(JsonParseException::new);
                remainders.put(item.value(), remainderStack);
            } else {
                var remainderItem = GsonHelper.convertToItem(remainderEntry.getValue(), "item");
                remainders.put(item.value(), new ItemStack(remainderItem));
            }
        }

        if (remainders.isEmpty()) return;
        RecipeRemainderStorage.store(entry.getKey(), remainders);
    }

    @Inject(method = "getRemainingItemsFor", at = @At(value = "RETURN", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private <I extends RecipeInput, R extends Recipe<I>> void addRecipeSpecificRemainders(RecipeType<R> type, I inventory, Level world, CallbackInfoReturnable<NonNullList<ItemStack>> cir, Optional<RecipeHolder<R>> optional) {
        if (optional.isEmpty() || !RecipeRemainderStorage.has(optional.get().id())) return;

        var remainders = cir.getReturnValue();
        var owoRemainders = RecipeRemainderStorage.get(optional.get().id());

        for (int i = 0; i < remainders.size(); ++i) {
            var item = inventory.getItem(i).getItem();
            if (!owoRemainders.containsKey(item)) continue;

            remainders.set(i, owoRemainders.get(item).copy());
        }
    }
}
