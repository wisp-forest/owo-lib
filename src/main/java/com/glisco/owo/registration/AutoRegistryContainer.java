package com.glisco.owo.registration;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.InvocationTargetException;

public interface AutoRegistryContainer<T> {

    Registry<T> getRegistry();

    Class<T> getRegisteredType();

    default void afterRegistration() {

    }

    @SuppressWarnings("unchecked")
    static <T> void register(Class<? extends AutoRegistryContainer<T>> containerClass, String targetModId) {
        AutoRegistryContainer<T> container;
        try {
            container = containerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not instantiate registry container", e);
        }

        for (var field : containerClass.getDeclaredFields()) {
            Object value;
            try {
                value = field.get(containerClass);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            if (!container.getRegisteredType().isAssignableFrom(value.getClass())) continue;

            var fieldId = field.getName().toLowerCase();
            if (field.isAnnotationPresent(RegisteredName.class)) fieldId = field.getAnnotation(RegisteredName.class).value();

            Registry.register(container.getRegistry(), new Identifier(targetModId, fieldId), (T) value);
        }

        container.afterRegistration();
    }

}
