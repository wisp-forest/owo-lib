package com.glisco.owo.registration.reflect;

import com.glisco.owo.registration.annotations.AssignedName;

/**
 * A class that can have its accessible static fields that match the
 * class of <b>T</b> processed by the {@link FieldRegistrationHandler}
 * <p>
 * <b>All implementations must provide a zero-args constructor</b>
 *
 * @param <T> The type of field to be processed
 */
public interface FieldProcessingSubject<T> {

    /**
     * @return The class of <b>T</b>
     */
    Class<T> getTargetFieldType();

    /**
     * Called to check if a given field should be processed
     *
     * @param value      The value the inspected field currently has
     * @param identifier The identifier that field was assigned, possibly overridden by an {@link AssignedName}
     *                   annotation and always fully lowercase
     * @return {@code true} if the inspected field should be processed
     */
    default boolean shouldProcessField(T value, String identifier) {
        return true;
    }

    /**
     * Called after all applicable fields of this class have been processed
     */
    default void afterFieldProcessing() {

    }

}
