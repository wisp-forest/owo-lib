package io.wispforest.owo.registration.reflect;

import io.wispforest.owo.registration.annotations.AssignedName;
import java.lang.reflect.Field;
import net.minecraft.core.Registry;

/**
 * A special version of {@link FieldProcessingSubject} that contains fields which should
 * be registered into a {@link Registry} using the field names in lowercase as ID
 * <p>
 * Use {@link #register(Class, String, boolean)} to automatically register all fields
 * of a given implementation into its specified registry
 *
 * @param <T> The type of objects to register, same as the Registry's type parameter
 */
public interface AutoRegistryContainer<T> extends FieldProcessingSubject<T> {

    /**
     * @return The registry the fields of this class should be registered into
     */
    Registry<T> getRegistry();

    /**
     * Called after the given field has been registered
     *
     * @param namespace  The namespace that is being used to register this class' fields
     * @param value      The value that was registered
     * @param identifier The identifier the field was assigned, possibly overridden by an {@link AssignedName}
     *                   annotation and always fully lowercase
     */
    default void postProcessField(String namespace, T value, String identifier, Field field) {}

    /**
     * Convenience-alias for {@link FieldRegistrationHandler#register(Class, String, boolean)}
     */
    static <T> void register(Class<? extends AutoRegistryContainer<T>> container, String namespace, boolean recurse) {
        FieldRegistrationHandler.register(container, namespace, recurse);
    }

    @SuppressWarnings({"unchecked"})
    static <T> Class<T> conform(Class<?> input) {
        return (Class<T>) input;
    }
}
