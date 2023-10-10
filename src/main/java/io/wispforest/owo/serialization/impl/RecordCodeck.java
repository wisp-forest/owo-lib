package io.wispforest.owo.serialization.impl;

import com.google.common.collect.ImmutableMap;
import io.wispforest.owo.Owo;
import io.wispforest.owo.serialization.*;
import org.apache.commons.lang3.mutable.MutableInt;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RecordCodeck<R extends Record> implements StructCodeck<R> {
    private static final Map<Class<?>, RecordCodeck<?>> SERIALIZERS = new HashMap<>();

    private final Map<String, RecordCodeck.RecordEntryHandler<R>> adapters;
    private final Class<R> recordClass;
    private final Constructor<R> instanceCreator;
    private final int fieldCount;

    private RecordCodeck(Class<R> recordClass, Constructor<R> instanceCreator, ImmutableMap<String, RecordCodeck.RecordEntryHandler<R>> adapters) {
        this.recordClass = recordClass;
        this.instanceCreator = instanceCreator;
        this.adapters = adapters;
        this.fieldCount = recordClass.getRecordComponents().length;
    }

    /**
     * Creates a new serializer for the given record type, or retrieves the
     * existing one if it was already created
     *
     * @param recordClass The type of record to (de-)serialize
     * @param <R>         The type of record to (de-)serialize
     * @return The serializer for the given record type
     */
    public static <R extends Record> RecordCodeck<R> create(Class<R> recordClass) {
        if (SERIALIZERS.containsKey(recordClass)) return (RecordCodeck<R>) SERIALIZERS.get(recordClass);

        final ImmutableMap.Builder<String, RecordCodeck.RecordEntryHandler<R>> handlerBuilder = new ImmutableMap.Builder<>();

        final Class<?>[] canonicalConstructorArgs = new Class<?>[recordClass.getRecordComponents().length];

        var lookup = MethodHandles.publicLookup();
        for (int i = 0; i < recordClass.getRecordComponents().length; i++) {
            try {
                var component = recordClass.getRecordComponents()[i];
                var handle = lookup.unreflect(component.getAccessor());

                handlerBuilder.put(component.getName(),
                        new RecordCodeck.RecordEntryHandler<>(
                                r -> getRecordEntry(r, handle),
                                ReflectionCodeckBuilder.getGeneric(component.getGenericType())
                        )
                );

                canonicalConstructorArgs[i] = component.getType();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not create method handle for record component");
            }
        }

        try {
            final var serializer = new RecordCodeck<>(recordClass, recordClass.getConstructor(canonicalConstructorArgs), handlerBuilder.build());
            SERIALIZERS.put(recordClass, serializer);
            return serializer;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not locate canonical record constructor");
        }
    }

    public Class<R> getRecordClass() {
        return recordClass;
    }

    private static <R extends Record> Object getRecordEntry(R instance, MethodHandle accessor) {
        try {
            return accessor.invoke(instance);
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to get record component value", e);
        }
    }

    /**
     * Attempts to read a record of this serializer's
     * type from the given buffer
     *
     * @return The deserialized record
     */
    @Override
    public R decode(StructDeserializer struct) {
        Object[] messageContents = new Object[fieldCount];

        var index = new MutableInt();

        adapters.forEach((s, fHandler) -> {
            messageContents[index.getAndIncrement()] = struct.field(s, fHandler.kodeck);
        });

        try {
            return instanceCreator.newInstance(messageContents);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Owo.LOGGER.error("Error while deserializing record", e);
        }

        return null;
    }

    /**
     * Writes the given record instance
     * to the given buffer
     *
     * @param instance The record instance to serialize
     */
    @Override
    public void encode(StructSerializer struct, R instance) {
        adapters.forEach((s, fHandler) -> struct.field(s, fHandler.kodeck, fHandler.rFunction.apply(instance)));
    }

    private record RecordEntryHandler<R>(Function<R, ?> rFunction, Codeck kodeck) { }
}
