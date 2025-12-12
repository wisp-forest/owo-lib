package io.wispforest.owo.mixin.extension.recipe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.wispforest.owo.util.RecipeRemainderStorage;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Function;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

    @Shadow
    @Final
    private Player player;

    @Inject(
        method = "onTake",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/CraftingContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V",
            ordinal = 1
        )
    )
    private void fixRemainderStacking(
        Player player,
        ItemStack stack,
        CallbackInfo ci,
        @Local(ordinal = 2) ItemStack remainderStack
    ) {
        if (remainderStack.getCount() > remainderStack.getMaxStackSize()) {
            int excess = remainderStack.getCount() - remainderStack.getMaxStackSize();
            remainderStack.shrink(excess);

            this.player.getInventory().placeItemBackInInventory(remainderStack.copyWithCount(excess));
        }
    }

    @WrapOperation(
        method = "getRemainingItems",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"
        )
    )
    private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> captureRecipeEntry(
        RecipeManager instance,
        RecipeType<T> type,
        I input,
        Level world,
        Operation<Optional<RecipeHolder<T>>> original,
        @Share(value = "recipe_entry") LocalRef<Optional<RecipeHolder<T>>> recipeEntry
    ) {
        var entry = original.call(instance, type, input, world);

        recipeEntry.set(entry);

        return entry;
    }

    @WrapOperation(
        method = "getRemainingItems",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;"
        )
    )
    private <I extends RecipeInput, T extends Recipe<I>> Optional<NonNullList<ItemStack>> addRecipeSpecificRemainders(
        Optional<T> instance,
        Function<? super T, ? extends NonNullList<ItemStack>> mapper,
        Operation<Optional<NonNullList<ItemStack>>> original,
        @Share(value = "recipe_entry") LocalRef<Optional<RecipeHolder<?>>> recipeEntry,
        @Local(argsOnly = true) CraftingInput input
    ) {
        var recipeEntryOptional = recipeEntry.get();

        return original.call(instance, mapper)
            .map(remainders -> {
                var recipeId = recipeEntryOptional.get().id().identifier();

                if (RecipeRemainderStorage.has(recipeId)) {
                    var owoRemainders = RecipeRemainderStorage.get(recipeId);

                    for (int i = 0; i < remainders.size(); ++i) {
                        var item = input.getItem(i).getItem();
                        if (!owoRemainders.containsKey(item)) continue;

                        remainders.set(i, owoRemainders.get(item).copy());
                    }
                }

                return remainders;
            });
    }
}
