package io.wispforest.owo.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
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

    public static void onServerStart(AddReloadListenerEvent event) {
        event.addListener((SynchronousResourceReloader) manager -> REMAINDERS.clear());
    }
}
