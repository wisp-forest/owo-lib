package io.wispforest.owo.util;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

@Deprecated
public final class RegistryAccess {

    private RegistryAccess() {}

    /**
     * @deprecated Use {@link Registry#getEntry(Identifier)}
     */
    @Nullable
    public static <T> RegistryEntry<T> getEntry(Registry<T> registry, Identifier id) {
        return registry.getEntry(id).orElse(null);
    }

    /**
     * @deprecated Use {@link Registry#getEntry(T)}
     */
    @Nullable
    public static <T> RegistryEntry<T> getEntry(Registry<T> registry, T value) {
        return registry.getEntry(value);
    }
}
