package io.wispforest.owo.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public final class RegistryAccess {

    private RegistryAccess() {}

    /**
     * @deprecated Use {@link Registry#getHolder(Identifier)}
     */
    @Nullable
    public static <T> Holder<T> getEntry(Registry<T> registry, Identifier id) {
        return registry.getHolder(id).orElse(null);
    }

    /**
     * @deprecated Use {@link Registry#wrapAsHolder(T)}
     */
    @Nullable
    public static <T> Holder<T> getEntry(Registry<T> registry, T value) {
        return registry.wrapAsHolder(value);
    }
}
