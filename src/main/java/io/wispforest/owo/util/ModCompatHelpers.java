package io.wispforest.owo.util;

import io.wispforest.owo.registration.RegistryHelper;
import net.minecraft.util.registry.Registry;

public class ModCompatHelpers {

    /**
     * @deprecated Use {@link RegistryHelper#get(Registry)} instead
     */
    @Deprecated(forRemoval = true)
    public static <T> RegistryHelper<T> getRegistryHelper(Registry<T> registry) {
        return RegistryHelper.get(registry);
    }

}
