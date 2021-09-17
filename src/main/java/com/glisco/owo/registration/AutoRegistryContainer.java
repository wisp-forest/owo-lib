package com.glisco.owo.registration;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * A class that contains objects to be registered into a {@link Registry}, using the field
 * names in lowercase as ID
 * <p>
 * Use {@link AutoRegistryContainer#register(Class, String)} to automatically register all fields
 * of this class into the specified registry
 *
 * @param <T> The type of objects to register, same as the Registry's type parameter
 */
public interface AutoRegistryContainer<T> {

    /**
     * @return The registry the fields of this class should be registered into
     */
    Registry<T> getRegistry();

    /**
     * @return The class of <b>T</b>
     */
    Class<T> getRegisteredType();

    /**
     * Called after the fields of this class have been registered,
     * implement if you want to register relevant objects
     */
    default void afterRegistration() {

    }

    /**
     * Registers all public static fields of the specified class that
     * match its type parameter into the registry it specifies
     *
     * @param containerClass The class from which to take the fields, must implement {@link AutoRegistryContainer}
     * @param targetModId    The namespace to use in the generated identifiers
     * @param <T>            The type of object to register
     */
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
            if (!Modifier.isStatic(field.getModifiers())) continue;

            Object value;
            try {
                value = field.get(containerClass);
            } catch (IllegalAccessException e) {
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
