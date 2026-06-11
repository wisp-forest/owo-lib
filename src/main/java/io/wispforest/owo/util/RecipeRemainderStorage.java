package io.wispforest.owo.util;

//import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class RecipeRemainderStorage {

    private RecipeRemainderStorage() {}

    private static final Map<Identifier, Map<Item, ItemStackTemplate>> REMAINDERS = new HashMap<>();

    public static void store(Identifier recipe, Map<Item, ItemStackTemplate> remainders) {
        REMAINDERS.put(recipe, remainders);
    }

    public static boolean has(Identifier recipe) {
        return REMAINDERS.containsKey(recipe);
    }

    public static Map<Item, ItemStackTemplate> get(Identifier recipe) {
        return REMAINDERS.get(recipe);
    }

    static {
        NeoForge.EVENT_BUS.<AddServerReloadListenersEvent>addListener((event) -> REMAINDERS.clear());
    }
}
