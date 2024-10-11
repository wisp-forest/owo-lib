package io.wispforest.owo.registration;

import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Experimental
@ApiStatus.Internal
public class ThreadedStorage {
    private static final Map<Class<?>, ThreadLocal<@Nullable RegistryKey<?>>> map = new ConcurrentHashMap<>();

    @Nullable
    public static <T> RegistryKey<T> getCurrentKey(Class<T> baseClazz) {
        var local = map.get(baseClazz);

        if (local == null) return null;

        var key = (RegistryKey<T>) local.get();

        local.remove();

        return key;
    }

    public static <T> void setCurrentKey(Class<T> baseClazz, RegistryKey<T> key) {
        map.computeIfAbsent(baseClazz, aClass -> ThreadLocal.withInitial(() -> null));
    }
}
