package io.wispforest.owo.registration;

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
 * entries are present or not. Use {@link #get(Registry)}
 * to obtain the instance for a given registry
 */
public class RegistryHelper<T> {

    private static final Map<Registry<?>, RegistryHelper<?>> INSTANCES = new HashMap<>();

    private final Registry<T> registry;
    private final Map<Identifier, Consumer<T>> actions = new HashMap<>();

    private final List<ComplexRegistryAction> complexActions = new ArrayList<>();

    /**
     * Gets the {@link RegistryHelper} instance for the provided registry
     *
     * @param registry The target registry
     * @return The helper for the targeted registry
     */
    @SuppressWarnings("unchecked")
    public static <T> RegistryHelper<T> get(Registry<T> registry) {
        return (RegistryHelper<T>) INSTANCES.computeIfAbsent(registry, objects -> new RegistryHelper<>(registry));
    }

    @ApiStatus.Internal
    public RegistryHelper(Registry<T> registry) {
        this.registry = registry;
        RegistryEntryAddedCallback.event(registry).register((rawId, id, object) -> {
            if (actions.containsKey(id)) {
                actions.remove(id).accept(object);
            }

            final var actionsToExecute = new ArrayList<Runnable>();
            complexActions.removeIf(action -> action.update(id, actionsToExecute));
            actionsToExecute.forEach(Runnable::run);
        });
    }

    /**
     * Runs the given consumer supplied with the registered object as soon
     * as the requested ID exists in the registry
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
     * Runs the given action once all of its required entries are
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
