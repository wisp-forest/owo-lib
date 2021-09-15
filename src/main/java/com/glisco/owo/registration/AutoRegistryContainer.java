package com.glisco.owo.registration;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.InvocationTargetException;

public interface AutoRegistryContainer<T> {

    Registry<T> getRegistry();

    Class<T> getRegisteredType();

    @SuppressWarnings("unchecked")
    static <T> void register(Class<? extends AutoRegistryContainer<T>> entrypointClass, String targetModId) {
        AutoRegistryContainer<T> entrypoint;
        try {
            entrypoint = entrypointClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not instantiate registry container", e);
        }

        for (var field : entrypointClass.getDeclaredFields()) {
            Object value;
            try {
                value = field.get(entrypointClass);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            if (!entrypoint.getRegisteredType().isAssignableFrom(value.getClass())) continue;

            var id = new Identifier(targetModId, field.getName().toLowerCase());
            Registry.register(entrypoint.getRegistry(), id, (T) value);
        }
    }

}
