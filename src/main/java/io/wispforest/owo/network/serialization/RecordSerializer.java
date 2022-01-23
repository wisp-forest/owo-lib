package io.wispforest.owo.network.serialization;

import com.google.common.collect.ImmutableMap;
import io.wispforest.owo.Owo;
import net.minecraft.network.PacketByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
 * using {@link PacketBufSerializer#register(Class, BiConsumer, Function)}
 *
 * <p> To serialize an instance use {@link #write(PacketByteBuf, Record)},
 * to read it back again use {@link #read(PacketByteBuf)}
 *
 * @param <R> The type of record this serializer can handle
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class RecordSerializer<R extends Record> {

    private static final Map<Class<?>, RecordSerializer<?>> SERIALIZERS = new HashMap<>();

    private final Map<Function<R, ?>, PacketBufSerializer> adapters;
    private final Class<R> recordClass;
    private final Constructor<R> instanceCreator;
    private final int fieldCount;

    private RecordSerializer(Class<R> recordClass, Constructor<R> instanceCreator, ImmutableMap<Function<R, ?>, PacketBufSerializer> adapters) {
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
    public static <R extends Record> RecordSerializer<R> create(Class<R> recordClass) {
        if (SERIALIZERS.containsKey(recordClass)) return (RecordSerializer<R>) SERIALIZERS.get(recordClass);

        final ImmutableMap.Builder<Function<R, ?>, PacketBufSerializer> adapters = new ImmutableMap.Builder<>();
        final Class<?>[] canonicalConstructorArgs = new Class<?>[recordClass.getRecordComponents().length];

        for (int i = 0; i < recordClass.getRecordComponents().length; i++) {
            var component = recordClass.getRecordComponents()[i];

            adapters.put(r -> getRecordEntry(r, component.getAccessor()), PacketBufSerializer.getWithAnnotations(component.getType(), component));
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

    public Class<R> getRecordClass() {
        return recordClass;
    }

    private static <R extends Record> Object getRecordEntry(R instance, Method accessor) {
        try {
            return accessor.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to get record entry", e);
        }
    }
}

