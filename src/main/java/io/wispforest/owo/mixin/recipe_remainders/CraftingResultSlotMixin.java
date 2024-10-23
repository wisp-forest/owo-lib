package io.wispforest.owo.mixin.recipe_remainders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wispforest.owo.util.RecipeRemainderStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.function.Function;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {

    @Shadow
    @Final
    private PlayerEntity player;

    @Inject(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/RecipeInputInventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void fixRemainderStacking(PlayerEntity player, ItemStack stack, CallbackInfo ci, CraftingRecipeInput.Positioned positioned, CraftingRecipeInput craftingRecipeInput, int i, int j, DefaultedList defaultedList, int k, int l, int m, ItemStack itemStack, ItemStack remainderStack) {
        if (remainderStack.getCount() > remainderStack.getMaxCount()) {
            int excess = remainderStack.getCount() - remainderStack.getMaxCount();
            remainderStack.decrement(excess);

            var insertStack = remainderStack.copy();
            insertStack.setCount(excess);

            if (!this.player.getInventory().insertStack(insertStack)) {
                this.player.dropItem(insertStack, false);
            }
        }
    }

    @WrapOperation(method = "getRecipeRemainders", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/ServerRecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/world/World;)Ljava/util/Optional;"))
    private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> captureRecipeEntry(ServerRecipeManager instance, RecipeType<T> type, I input, World world, Operation<Optional<RecipeEntry<T>>> original, @Share("owo_recipe_entry") LocalRef<Optional<RecipeEntry<T>>> recipeEntry) {
        var entry = original.call(instance, type, input, world);

        recipeEntry.set(entry);

        return entry;
    }

    @WrapOperation(method = "getRecipeRemainders", at = @At(value = "INVOKE", target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;"))
    private <I extends RecipeInput, T extends Recipe<I>> Optional<DefaultedList<ItemStack>> addRecipeSpecificRemainders(Optional<T> instance, Function<? super T, ? extends DefaultedList<ItemStack>> mapper, Operation<Optional<DefaultedList<ItemStack>>> original, @Share("owo_recipe_entry") LocalRef<Optional<RecipeEntry<?>>> recipeEntry, @Local(argsOnly = true) CraftingRecipeInput input) {
        var recipeEntryOptional = recipeEntry.get();

        return original.call(instance, mapper)
                .map(defaultList -> {
                    var recipeId = recipeEntryOptional.get().id().getValue();

                    if(RecipeRemainderStorage.has(recipeId)) {
                        var remainders = defaultList;
                        var owoRemainders = RecipeRemainderStorage.get(recipeId);

                        for (int i = 0; i < remainders.size(); ++i) {
                            var item = input.getStackInSlot(i).getItem();
                            if (!owoRemainders.containsKey(item)) continue;

                            remainders.set(i, owoRemainders.get(item).copy());
                        }
                    }

                    return defaultList;
                });
    }
}
