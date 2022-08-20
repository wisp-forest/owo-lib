package io.wispforest.owo.mixin.recipe_remainders;

import io.wispforest.owo.util.RecipeRemainderStorage;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Recipe.class)
public interface RecipeMixin<C extends Inventory> {

    @Shadow
    Identifier getId();

    @Inject(method = "getRemainder", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addRecipeSpecificRemainders(C inventory, CallbackInfoReturnable<DefaultedList<ItemStack>> cir, DefaultedList<ItemStack> remainders) {
        if (!RecipeRemainderStorage.has(this.getId())) return;

        var owoRemainders = RecipeRemainderStorage.get(this.getId());
        for (int i = 0; i < remainders.size(); ++i) {
            var item = inventory.getStack(i).getItem();
            if (!owoRemainders.containsKey(item)) continue;

            remainders.set(i, owoRemainders.get(item).copy());
        }
    }
}