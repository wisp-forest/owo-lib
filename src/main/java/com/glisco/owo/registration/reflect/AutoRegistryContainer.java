package com.glisco.owo.registration.reflect;

import net.minecraft.util.registry.Registry;

/**
 * A special version of {@link FieldProcessingSubject} that contains fields which should
 * be registered into a {@link Registry} using the field names in lowercase as ID
 *
 * <p>
 * Use {@link FieldRegistrationHandler#register(Class, String, boolean)} to automatically register all fields
 * of this class into the specified registry
 *
 * @param <T> The type of objects to register, same as the Registry's type parameter
 */
public interface AutoRegistryContainer<T> extends FieldProcessingSubject<T> {

    /**
     * @return The registry the fields of this class should be registered into
     */
    Registry<T> getRegistry();
}
