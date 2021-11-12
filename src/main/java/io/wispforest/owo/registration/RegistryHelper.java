package io.wispforest.owo.registration;

import io.wispforest.owo.util.ModCompatHelpers;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A simple helper to run code conditionally based on whether certain registry
 * entries are present or not. Use {@link ModCompatHelpers#getRegistryHelper(Registry)}
 * to obtain the instance for a given registry
 */
public class RegistryHelper<T> {

    private final Registry<T> registry;
    private final Map<Identifier, Consumer<T>> actions;

    private final List<ComplexRegistryAction> complexActions = new ArrayList<>();

    @ApiStatus.Internal
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

    /**
     * Runs the given consumer supplied with the registered object as soon
     * as the requested id exists in the registry
     *
     * @param id     The ID the registry must contain for {@code action} to be run
     * @param action The code to run once {@code id} is present
     */
    public void runWhenPresent(Identifier id, Consumer<T> action) {
        if (isContained(registry, id)) {
            action.accept(registry.get(id));
        } else {
            this.actions.put(id, action);
        }
    }

    /**
     * Runs the given action once all of it's required entries are
     * present in the registry
     *
     * @param action The {@link ComplexRegistryAction} to run or queue
     */
    public void runWhenPresent(ComplexRegistryAction action) {
        if (!action.preCheck(registry)) {
            this.complexActions.add(action);
        }
    }

    private static <T> boolean isContained(Registry<T> registry, Identifier identifier) {
        return registry.containsId(identifier);
    }

}
