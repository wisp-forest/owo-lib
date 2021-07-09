package com.glisco.owo.util;

import com.glisco.owo.registration.RegistryHelper;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class ModCompatHelpers {

    private static final Map<Registry<?>, RegistryHelper<?>> REGISTRY_HELPERS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> RegistryHelper<T> getRegistryHelper(Registry<T> registry) {
        return (RegistryHelper<T>) REGISTRY_HELPERS.computeIfAbsent(registry, objects -> new RegistryHelper<>(registry));
    }

}
