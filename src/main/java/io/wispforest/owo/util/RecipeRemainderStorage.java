package io.wispforest.owo.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    static {
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> REMAINDERS.clear());
    }
}
