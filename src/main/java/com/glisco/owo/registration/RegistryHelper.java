package com.glisco.owo.registration;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RegistryHelper<T> {

    private final Registry<T> registry;
    private final Map<Identifier, Consumer<T>> actions;

    private final List<ComplexRegistryAction> complexActions = new ArrayList<>();

    public RegistryHelper(Registry<T> registry) {
        this.registry = registry;
        this.actions = new HashMap<>();

        RegistryEntryAddedCallback.event(registry).register((rawId, id, object) -> {
            if (actions.containsKey(id)) {
                actions.get(id).accept(object);
                actions.remove(id);
            }

            complexActions.removeIf(action -> action.update(id));
        });
    }

    public void runWhenPresent(Identifier id, Consumer<T> action) {

        if (isContained(registry, id)) {
            action.accept(registry.get(id));
        } else {
            this.actions.put(id, action);
        }

    }

    public void runWhenPresent(ComplexRegistryAction action) {
        if (!action.preCheck(registry)) {
            this.complexActions.add(action);
        }
    }

    public static <T> boolean isContained(Registry<T> registry, Identifier identifier) {
        return registry.containsId(identifier);
    }

}
