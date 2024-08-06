package io.wispforest.owo.mixin.recipe_remainders;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {

    @Shadow
    @Final
    private Player player;

    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/CraftingContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void fixRemainderStacking(Player player, ItemStack stack, CallbackInfo ci, CraftingInput.Positioned positioned, CraftingInput craftingRecipeInput, int i, int j, NonNullList defaultedList, int k, int l, int m, ItemStack itemStack, ItemStack remainderStack) {
        if (remainderStack.getCount() > remainderStack.getMaxStackSize()) {
            int excess = remainderStack.getCount() - remainderStack.getMaxStackSize();
            remainderStack.shrink(excess);

            var insertStack = remainderStack.copy();
            insertStack.setCount(excess);

            if (!this.player.getInventory().add(insertStack)) {
                this.player.drop(insertStack, false);
            }
        }
    }

}
