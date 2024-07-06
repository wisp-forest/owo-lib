package io.wispforest.owo.util;

import io.wispforest.owo.mixin.registry.SimpleRegistryAccessor;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;

public final class RegistryAccess {

    private RegistryAccess() {}

    /**
     * Gets a {@link RegistryEntry} from its id
     *
     * @param registry The registry to operate on. Must be a {@link SimpleRegistry} at some point in the hierarchy
     * @param id       The id to use
     * @param <T>      The type of the registry and returned entry
     * @return The entry, or {@code null} if it's not present
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> RegistryEntry<T> getEntry(Registry<T> registry, Identifier id) {
        checkSimple(registry);
        return ((SimpleRegistryAccessor<T>) registry).owo$getIdToEntry().get(id);
    }

    /**
     * Gets a {@link RegistryEntry} from its value
     *
     * @param registry The registry to operate on. Must be a {@link SimpleRegistry} at some point in the hierarchy
     * @param value    The value to use
     * @param <T>      The type of the registry and returned entry
     * @return The entry, or {@code null} if it's not present
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> RegistryEntry<T> getEntry(Registry<T> registry, T value) {
        checkSimple(registry);
        return ((SimpleRegistryAccessor<T>) registry).owo$getValueToEntry().get(value);
    }

    private static void checkSimple(Registry<?> registry) {
        if (registry instanceof SimpleRegistry<?>) return;
        throw new IllegalArgumentException("[RegistryAccess] Tried to operate on Registry of class '"
                + registry.getClass() + "', but only 'SimpleRegistry' and descendants are supported");
    }
}
