package io.wispforest.owo.mixin.recipe_remainders;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.util.RecipeRemainderStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.jetbrains.annotations.Nullable;
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

    public final ThreadLocal<Map.Entry<Identifier, JsonElement>> previousMapEntry = ThreadLocal.withInitial(() -> null);

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresentOrElse(Ljava/util/function/Consumer;Ljava/lang/Runnable;)V"))
    private void deserializeRecipeSpecificRemainders(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci, @Local Map.Entry<Identifier, JsonElement> entry, @Local Optional<WithConditions<Recipe<?>>> decoded) {
        if(decoded.isEmpty()) return;

        var json = entry.getValue().getAsJsonObject();
        if (!json.has("owo:remainders")) return;

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

        if (remainders.isEmpty()) return;
        RecipeRemainderStorage.store(entry.getKey(), remainders);
    }

    @Inject(method = "getRemainingStacks", at = @At(value = "RETURN", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private <I extends RecipeInput, R extends Recipe<I>> void addRecipeSpecificRemainders(RecipeType<R> type, I inventory, World world, CallbackInfoReturnable<DefaultedList<ItemStack>> cir, Optional<RecipeEntry<R>> optional) {
        if (optional.isEmpty() || !RecipeRemainderStorage.has(optional.get().id())) return;

        var remainders = cir.getReturnValue();
        var owoRemainders = RecipeRemainderStorage.get(optional.get().id());

        for (int i = 0; i < remainders.size(); ++i) {
            var item = inventory.getStackInSlot(i).getItem();
            if (!owoRemainders.containsKey(item)) continue;

            remainders.set(i, owoRemainders.get(item).copy());
        }
    }
}
