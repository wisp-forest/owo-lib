package io.wispforest.owo.registration.reflect;

import io.wispforest.owo.registration.annotations.AssignedName;

import java.lang.reflect.Field;

/**
 * A simpler to use version of {@link FieldProcessingSubject} that
 * provides the processor to apply to its fields
 *
 * @param <T>
 */
public interface SimpleFieldProcessingSubject<T> extends FieldProcessingSubject<T> {

    /**
     * Processes the given field
     *
     * @param value      The value of the inspected field at the time this method is called
     * @param identifier The identifier that field was assigned, either it's name in lowercase or specified
     *                   by an {@link AssignedName} annotation
     */
    void processField(T value, String identifier, Field field);
}
