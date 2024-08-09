package io.wispforest.owo.registration.reflect;

import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.reflect.entry.MemoizedEntry;
import io.wispforest.owo.registration.reflect.entry.TypedRegistryEntry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * A special version of {@link FieldProcessingSubject} that contains fields which should
 * be registered into a {@link Registry} using the field names in lowercase as ID
 * <p>
 * Use {@link #register(Class, String, boolean)} to automatically register all fields
 * of a given implementation into its specified registry
 *
 * @param <T> The type of objects to register, same as the Registry's type parameter
 */
public abstract class AutoRegistryContainer<T> implements FieldProcessingSubject<T> {

    /**
     * @return The registry the fields of this class should be registered into
     */
    public abstract Registry<T> getRegistry();

    /**
     * Called after the given field has been registered
     *
     * @param namespace  The namespace that is being used to register this class' fields
     * @param value      The value that was registered
     * @param identifier The identifier the field was assigned, possibly overridden by an {@link AssignedName}
     *                   annotation and always fully lowercase
     */
    public void postProcessField(String namespace, T value, String identifier, Field field) {}

    /**
     * Convenience-alias for {@link FieldRegistrationHandler#register(Class, String, boolean)}
     */
    public static <T> void register(Class<? extends AutoRegistryContainer<T>> container, String namespace, boolean recurse) {
        FieldRegistrationHandler.register(container, namespace, recurse);
    }

    @SuppressWarnings({"unchecked"})
    protected static <T> Class<T> conform(Class<?> input) {
        return (Class<T>) input;
    }

    public static <T> RegistryEntry<T> entry(Supplier<T> supplier) {
        return MemoizedEntry.ofEntry(supplier);
    }

    public static <T extends B, B> TypedRegistryEntry<T, B> typedEntry(Supplier<T> supplier) {
        return MemoizedEntry.ofTypedEntry(supplier);
    }
}
