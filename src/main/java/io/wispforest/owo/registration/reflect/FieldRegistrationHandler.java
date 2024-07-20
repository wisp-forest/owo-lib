package io.wispforest.owo.registration.reflect;

import io.wispforest.owo.registration.annotations.IterationIgnored;
import io.wispforest.owo.registration.annotations.RegistryNamespace;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

/**
 * Main hub for all interactions with implementations of
 * {@link FieldProcessingSubject}
 */
@SuppressWarnings("unchecked")
public final class FieldRegistrationHandler {

    private FieldRegistrationHandler() {}

    /**
     * Applies the given processor to all applicable fields of the targeted class
     *
     * @param clazz                   The class to target, must implement {@link FieldProcessingSubject}
     * @param processor               The function to apply to each applicable field
     * @param recurseIntoInnerClasses Whether this method should recursively process all inner classes of {@code clazz}
     * @param <T>                     The type of field to match
     */
    public static <T> void process(Class<? extends FieldProcessingSubject<T>> clazz, ReflectionUtils.FieldConsumer<T> processor, boolean recurseIntoInnerClasses) {
        var handler = ReflectionUtils.tryInstantiateWithNoArgs(clazz);
        iterateAccessibleStaticFieldsAllowingMemorizedSuppliers(clazz, handler.getTargetFieldType(), createProcessor(processor, handler));

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
        iterateAccessibleStaticFieldsAllowingMemorizedSuppliers(clazz, handler.getTargetFieldType(), createProcessor(handler::processField, handler));

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

        iterateAccessibleStaticFieldsAllowingMemorizedSuppliers(clazz, container.getTargetFieldType(), createProcessor((fieldValue, identifier, field) -> {
            var reference = Registry.registerReference(container.getRegistry(), Identifier.of(namespace, identifier), fieldValue);

            try {
                var object = field.get(null);

                if(object instanceof MemoizedRegistryEntry memorizedRegistryEntry) {
                    memorizedRegistryEntry.setEntry(reference);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            container.postProcessField(namespace, fieldValue, identifier, field);
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

    private static <T> ReflectionUtils.FieldConsumer<T> createProcessor(ReflectionUtils.FieldConsumer<T> delegate, FieldProcessingSubject<T> handler) {
        return (value, name, field) -> {
            if (!handler.shouldProcessField(value, name, field)) return;
            delegate.accept(value, name, field);
        };
    }

    private static <C, F> void iterateAccessibleStaticFieldsAllowingMemorizedSuppliers(Class<C> clazz, Class<F> targetFieldType, ReflectionUtils.FieldConsumer<F> fieldConsumer) {
        for (var field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (field.isAnnotationPresent(IterationIgnored.class)) continue;

            Object fieldValue;
            try {
                fieldValue = field.get(null);
            } catch (IllegalAccessException e) {
                continue;
            }

            if(fieldValue == null) continue;

            var valueType = fieldValue.getClass();

            F finalValue = null;

            if (!targetFieldType.isAssignableFrom(valueType)) {
                boolean isValid = false;

                if(Supplier.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType parameterizedType) {
                    var genericType = parameterizedType.getActualTypeArguments()[0];

                    if (genericType instanceof Class<?> genericClass && targetFieldType.isAssignableFrom(genericClass)) {
                        if(!(fieldValue instanceof MemoizedEntry<?>)) {
                            throw new IllegalStateException("A given Supplier object must be of a memoized variant or problems may occur! [Field: " + field.getName() + "]");
                        }

                        finalValue = (F) ((Supplier) fieldValue).get();


                        isValid = true;
                    }
                }

                if(!isValid) continue;
            } else {
                finalValue = (F) fieldValue;
            }

            fieldConsumer.accept(finalValue, ReflectionUtils.getFieldName(field), field);
        }
    }

}
