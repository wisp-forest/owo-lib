package com.glisco.owo.registration.reflect;

import com.glisco.owo.registration.annotations.RegistryNamespace;
import com.glisco.owo.util.ReflectionUtils;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.BiConsumer;

/**
 * Main hub for all interactions with implementations of
 * {@link FieldProcessingSubject}
 */
@SuppressWarnings("unchecked")
public class FieldRegistrationHandler {

    /**
     * Applies the given processor to all applicable fields of the targeted class
     *
     * @param clazz                   The class to target, must implement {@link FieldProcessingSubject}
     * @param processor               The function to apply to each applicable field
     * @param recurseIntoInnerClasses Whether this method should recursively process all inner classes of {@code clazz}
     * @param <T>                     The type of field to match
     */
    public static <T> void process(Class<? extends FieldProcessingSubject<T>> clazz, BiConsumer<T, String> processor, boolean recurseIntoInnerClasses) {
        var handler = ReflectionUtils.tryInstantiateWithNoArgs(clazz);
        ReflectionUtils.iterateAccessibleStaticFields(clazz, handler.getTargetFieldType(), createProcessor(processor, handler));

        if (recurseIntoInnerClasses) {
            ReflectionUtils.forApplicableSubclasses(clazz, FieldProcessingSubject.class,
                    subclass -> process((Class<? extends FieldProcessingSubject<T>>) subclass, processor, true));
        }

        handler.afterFieldProcessing();
    }

    /**
     * Processes all fields of the given class with the implementation of
     * {@code processField(T, String)} it provides
     *
     * @param clazz                   The class to target, must implement {@link SimpleFieldProcessingSubject}
     * @param recurseIntoInnerClasses Whether this method should recursively process all inner classes of {@code clazz}
     * @param <T>                     The type of field to match
     */
    public static <T> void processSimple(Class<? extends SimpleFieldProcessingSubject<T>> clazz, boolean recurseIntoInnerClasses) {
        var handler = ReflectionUtils.tryInstantiateWithNoArgs(clazz);
        ReflectionUtils.iterateAccessibleStaticFields(clazz, handler.getTargetFieldType(), createProcessor(handler::processField, handler));

        if (recurseIntoInnerClasses) {
            ReflectionUtils.forApplicableSubclasses(clazz, SimpleFieldProcessingSubject.class,
                    subclass -> processSimple((Class<? extends SimpleFieldProcessingSubject<T>>) subclass, true));
        }

        handler.afterFieldProcessing();
    }

    /**
     * Registers all public static fields of the specified class that
     * match its type parameter into the registry it specifies
     *
     * @param clazz     The class from which to take the fields, must implement {@link AutoRegistryContainer}
     * @param namespace The namespace to use in the generated identifiers
     * @param <T>       The type of object to register
     */
    public static <T> void register(Class<? extends AutoRegistryContainer<T>> clazz, String namespace, boolean recurseIntoInnerClasses) {
        AutoRegistryContainer<T> container = ReflectionUtils.tryInstantiateWithNoArgs(clazz);

        ReflectionUtils.iterateAccessibleStaticFields(clazz, container.getTargetFieldType(), createProcessor((fieldValue, identifier) -> {
            Registry.register(container.getRegistry(), new Identifier(namespace, identifier), fieldValue);
            container.postProcessField(namespace, fieldValue, identifier);
        }, container));

        if (recurseIntoInnerClasses) {
            ReflectionUtils.forApplicableSubclasses(clazz, AutoRegistryContainer.class, subclass -> {
                var classModId = namespace;
                if (subclass.isAnnotationPresent(RegistryNamespace.class)) classModId = subclass.getAnnotation(RegistryNamespace.class).value();
                register((Class<? extends AutoRegistryContainer<T>>) subclass, classModId, true);
            });
        }

        container.afterFieldProcessing();
    }

    private static <T> BiConsumer<T, String> createProcessor(BiConsumer<T, String> delegate, FieldProcessingSubject<T> handler) {
        return (t, u) -> {
            if (!handler.shouldProcessField(t, u)) return;
            delegate.accept(t, u);
        };
    }

}
