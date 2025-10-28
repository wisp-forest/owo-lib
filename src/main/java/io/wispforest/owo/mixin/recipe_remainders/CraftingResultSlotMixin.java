package io.wispforest.owo.mixin.recipe_remainders;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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

}
