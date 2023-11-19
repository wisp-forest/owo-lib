package io.wispforest.uwu.mixin;

import net.minecraft.recipe.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeInvoker {
    @Invoker("removePadding")
    static String[] owo$removePadding(List<String> pattern) {
        throw new UnsupportedOperationException();
    }
}
