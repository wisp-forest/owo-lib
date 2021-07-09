package com.glisco.owo.util;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class RegistryHelper<T> {

    private static final List<Registry<?>> addedListeners = new ArrayList<>();
    private static final Map<Registry<?>, Map<Identifier, Consumer<?>>> actions = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> void runWhenPresent(Registry<T> registry, Identifier id, Consumer<T> action) {

        if(isContained(registry, id)){
            action.accept(registry.get(id));
        } else {

            if(!addedListeners.contains(registry)){
                RegistryEntryAddedCallback.event(registry).register((rawId, id1, object) -> {
                    final var registryActions = actions.get(registry);
                    if (registryActions.containsKey(id1)){
                        ((Consumer<T>) registryActions.get(id1)).accept(object);
                        registryActions.remove(id1);
                    }
                });
            }

            actions.computeIfAbsent(registry, objects -> new HashMap<>()).put(id, action);
        }

    }

    private static <T> boolean isContained(Registry<T> registry, Identifier identifier){
        return registry.containsId(identifier);
    }

}
