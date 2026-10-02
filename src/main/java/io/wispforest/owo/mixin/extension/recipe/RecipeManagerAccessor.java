package io.wispforest.owo.mixin.extension.recipe;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The finder is now created by FileToIdConverter.registry, outside this pure accessor mixin. */
@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("recipes")
    RecipeMap owo$getRecipes();
}
