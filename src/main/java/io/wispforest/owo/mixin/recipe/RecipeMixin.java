package io.wispforest.owo.mixin.recipe;

import io.wispforest.owo.util.RecipeSpecificRemainders;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
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

import java.util.Set;

@Mixin(Recipe.class)
public interface RecipeMixin<C extends Inventory> {

    @Shadow Identifier getId();

    @Inject(method = "getRemainder", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addRecipeSpecificRemainders(C inventory, CallbackInfoReturnable<DefaultedList<ItemStack>> cir, DefaultedList<ItemStack> defaultedList) {
        final var remainders = RecipeSpecificRemainders.getRemainders(getId());

        if(remainders.isEmpty()) return;

        for (int i = 0; i < defaultedList.size(); ++i) {
            Item item = inventory.getStack(i).getItem();

            for(RecipeSpecificRemainders.RecipeRemainder recipeRemainder : remainders) {
                if(recipeRemainder.item() == item){
                    defaultedList.set(i, new ItemStack(recipeRemainder.itemRemainder()));
                }
            }
        }
    }
}
