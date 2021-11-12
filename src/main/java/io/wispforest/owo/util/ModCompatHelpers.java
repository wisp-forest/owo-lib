package io.wispforest.owo.util;

import io.wispforest.owo.registration.RegistryHelper;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class ModCompatHelpers {

    private static final Map<Registry<?>, RegistryHelper<?>> REGISTRY_HELPERS = new HashMap<>();

    /**
     * Gets the {@link RegistryHelper} instance for the provided registry
     *
     * @param registry The target registry
     * @return The helper for the targeted registry
     */
    @SuppressWarnings("unchecked")
    public static <T> RegistryHelper<T> getRegistryHelper(Registry<T> registry) {
        return (RegistryHelper<T>) REGISTRY_HELPERS.computeIfAbsent(registry, objects -> new RegistryHelper<>(registry));
    }

}
