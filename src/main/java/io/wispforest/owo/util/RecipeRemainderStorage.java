package io.wispforest.owo.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.resource.VanillaServerListeners;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class RecipeRemainderStorage {

    private RecipeRemainderStorage() {}

    private static final Map<Identifier, Map<Item, ItemStack>> REMAINDERS = new HashMap<>();

    public static void store(Identifier recipe, Map<Item, ItemStack> remainders) {
        REMAINDERS.put(recipe, remainders);
    }

    public static boolean has(Identifier recipe) {
        return REMAINDERS.containsKey(recipe);
    }

    public static Map<Item, ItemStack> get(Identifier recipe) {
        return REMAINDERS.get(recipe);
    }

    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.of("owo", "reload_hook_recipe_remainders"), (SynchronousResourceReloader) manager -> REMAINDERS.clear());
        event.addDependency(Identifier.of("owo", "reload_hook_recipe_remainders"), VanillaServerListeners.RECIPES);
    }
}
