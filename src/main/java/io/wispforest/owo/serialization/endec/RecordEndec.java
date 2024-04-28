package io.wispforest.owo.serialization.endec;

import io.wispforest.owo.Owo;
import io.wispforest.owo.serialization.*;
import io.wispforest.owo.serialization.annotations.NullableComponent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecordEndec<R extends Record> implements StructEndec<R> {

    private static final Map<Class<?>, RecordEndec<?>> ENDECS = new HashMap<>();

    private final List<StructField<R, ?>> fields;
    private final Constructor<R> instanceCreator;

    private RecordEndec(Constructor<R> instanceCreator, List<StructField<R, ?>> fields) {
        this.instanceCreator = instanceCreator;
        this.fields = fields;
    }

    /**
     * Create (or get, if it already exists) the endec for the given record type
     */
    @SuppressWarnings("unchecked")
    public static <R extends Record> RecordEndec<R> create(Class<R> recordClass) {
        if (ENDECS.containsKey(recordClass)) return (RecordEndec<R>) ENDECS.get(recordClass);

        var fields = new ArrayList<StructField<R, ?>>();
        var canonicalConstructorArgs = new Class<?>[recordClass.getRecordComponents().length];

        var lookup = MethodHandles.publicLookup();
        for (int i = 0; i < recordClass.getRecordComponents().length; i++) {
            try {
                var component = recordClass.getRecordComponents()[i];
                var handle = lookup.unreflect(component.getAccessor());

                var endec = (Endec<Object>) ReflectiveEndecBuilder.get(component.getGenericType());
                if(component.isAnnotationPresent(NullableComponent.class)) endec = endec.nullableOf();

                fields.add(new StructField<>(
                        component.getName(),
                        endec,
                        instance -> getRecordEntry(instance, handle)
                ));

                canonicalConstructorArgs[i] = component.getType();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to create method handle for record component accessor");
            }
        }

        try {
            var endec = new RecordEndec<>(recordClass.getConstructor(canonicalConstructorArgs), fields);
            ENDECS.put(recordClass, endec);

            return endec;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not locate canonical record constructor");
        }
    }

    private static <R extends Record> Object getRecordEntry(R instance, MethodHandle accessor) {
        try {
            return accessor.invoke(instance);
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to get record component value", e);
        }
    }

    @Override
    public R decodeStruct(SerializationContext ctx, Deserializer.Struct struct) {
        Object[] fieldValues = new Object[this.fields.size()];

        var index = new MutableInt();

        this.fields.forEach((field) -> {
            fieldValues[index.getAndIncrement()] = field.decodeField(ctx, struct);
        });

        try {
            return instanceCreator.newInstance(fieldValues);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Owo.LOGGER.error("Error while deserializing record", e);
        }

        return null;
    }

    @Override
    public void encodeStruct(SerializationContext ctx, Serializer.Struct struct, R instance) {
        this.fields.forEach(field -> field.encodeField(ctx, struct, instance));
    }
}
