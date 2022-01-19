package io.wispforest.owo.network.serialization;

import com.google.common.collect.ImmutableMap;
import io.wispforest.owo.Owo;
import io.wispforest.owo.network.annotations.CollectionType;
import io.wispforest.owo.network.annotations.MapTypes;
import net.minecraft.network.PacketByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A utility for serializing {@code record} classes into {@link PacketByteBuf}s.
 * Use {@link #create(Class)} to create (or obtain if it already exists)
 * the instance for a specific class. Should an exception
 * about a missing type adapter be thrown, register one
 * using {@link TypeAdapter#register(Class, BiConsumer, Function)}
 *
 * <p> To serialize an instance use {@link #write(PacketByteBuf, Record)},
 * to read it back again use {@link #read(PacketByteBuf)}
 *
 * @param <R> The type of record this serializer can handle
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class RecordSerializer<R extends Record> {

    private static final Map<Class<?>, RecordSerializer<?>> SERIALIZERS = new HashMap<>();

    private final Map<Function<R, ?>, TypeAdapter> adapters;
    private final Constructor<R> instanceCreator;
    private final int fieldCount;

    private RecordSerializer(Class<R> recordClass, Constructor<R> instanceCreator, ImmutableMap<Function<R, ?>, TypeAdapter> adapters) {
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
    public static <R extends Record> RecordSerializer<R> create(Class<R> recordClass) {
        if (SERIALIZERS.containsKey(recordClass)) return (RecordSerializer<R>) SERIALIZERS.get(recordClass);

        final ImmutableMap.Builder<Function<R, ?>, TypeAdapter> adapters = new ImmutableMap.Builder<>();
        final Class<?>[] canonicalConstructorArgs = new Class<?>[recordClass.getRecordComponents().length];

        for (int i = 0; i < recordClass.getRecordComponents().length; i++) {
            var component = recordClass.getRecordComponents()[i];

            adapters.put(r -> getRecordEntry(r, component.getAccessor()), createAdapter(component.getType(), component));
            canonicalConstructorArgs[i] = component.getType();
        }

        try {
            final var serializer = new RecordSerializer<>(recordClass, recordClass.getConstructor(canonicalConstructorArgs), adapters.build());
            SERIALIZERS.put(recordClass, serializer);
            return serializer;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not locate canonical record constructor");
        }
    }

    /**
     * Attempts to read a record of this serializer's
     * type from the given buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized record
     */
    public R read(PacketByteBuf buffer) {
        Object[] messageContents = new Object[fieldCount];

        AtomicInteger index = new AtomicInteger();
        adapters.forEach((rFunction, typeAdapter) -> messageContents[index.getAndIncrement()] = typeAdapter.deserializer().apply(buffer));

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
     * @param buffer   The buffer to write to
     * @param instance The record instance to serialize
     */
    public RecordSerializer<R> write(PacketByteBuf buffer, R instance) {
        adapters.forEach((rFunction, typeAdapter) -> typeAdapter.serializer().accept(buffer, rFunction.apply(instance)));
        return this;
    }

    private static <R extends Record> Object getRecordEntry(R instance, Method accessor) {
        try {
            return accessor.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to get record entry", e);
        }
    }

    private static <T> TypeAdapter<T> createAdapter(Class<T> componentClass, RecordComponent component) {
        if (Map.class.isAssignableFrom(componentClass)) {
            var typeAnnotation = component.getAnnotation(MapTypes.class);
            return (TypeAdapter<T>) TypeAdapter.createMapAdapter(conform(componentClass, Map.class), typeAnnotation.keys(), typeAnnotation.values());
        }

        if (Collection.class.isAssignableFrom(componentClass)) {
            var typeAnnotation = component.getAnnotation(CollectionType.class);
            return (TypeAdapter<T>) TypeAdapter.createCollectionAdapter(conform(componentClass, Collection.class), typeAnnotation.value());
        }

        if (Record.class.isAssignableFrom(componentClass)) return (TypeAdapter<T>) TypeAdapter.createRecordAdapter(conform(componentClass, Record.class));
        if (componentClass.isEnum()) return (TypeAdapter<T>) TypeAdapter.createEnumAdapter(conform(componentClass, Enum.class));
        if (componentClass.isArray()) return (TypeAdapter<T>) TypeAdapter.createArrayAdapter(componentClass.getComponentType());

        return TypeAdapter.get(componentClass);
    }

    private static <T> Class<T> conform(Class<?> clazz, Class<T> target) {
        return (Class<T>) clazz;
    }

}

