package io.wispforest.owo.network.serialization;

import com.google.common.collect.ImmutableMap;
import io.wispforest.owo.network.annotations.CollectionType;
import io.wispforest.owo.network.annotations.MapTypes;
import io.wispforest.owo.util.VectorSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RecordSerializer<R extends Record> {

    private static final Map<Class<?>, TypeAdapter<?>> TYPE_ADAPTERS = new HashMap<>();

    private final Map<Function<R, ?>, TypeAdapter> adapters;
    private final Constructor<R> instanceCreator;
    private final int fieldCount;

    private RecordSerializer(Class<R> recordClass, Constructor<R> instanceCreator, ImmutableMap<Function<R, ?>, TypeAdapter> adapters) {
        this.instanceCreator = instanceCreator;
        this.adapters = adapters;
        this.fieldCount = recordClass.getRecordComponents().length;
    }

    public static <R extends Record> RecordSerializer<R> create(Class<R> recordClass) {
        final ImmutableMap.Builder<Function<R, ?>, TypeAdapter> adapters = new ImmutableMap.Builder<>();
        final Class<?>[] canonicalConstructorArgs = new Class<?>[recordClass.getRecordComponents().length];

        for (int i = 0; i < recordClass.getRecordComponents().length; i++) {
            var component = recordClass.getRecordComponents()[i];

            adapters.put(r -> getRecordEntry(r, component.getAccessor()), createAdapter(component.getType(), component));
            canonicalConstructorArgs[i] = component.getType();
        }

        try {
            return new RecordSerializer<>(recordClass, recordClass.getConstructor(canonicalConstructorArgs), adapters.build());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not locate canonical record constructor");
        }
    }

    public R read(PacketByteBuf buffer) {
        Object[] messageContents = new Object[fieldCount];

        AtomicInteger index = new AtomicInteger();
        adapters.forEach((rFunction, typeAdapter) -> messageContents[index.getAndIncrement()] = typeAdapter.deserializer().apply(buffer));

        try {
            return instanceCreator.newInstance(messageContents);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void write(PacketByteBuf buffer, R instance) {
        adapters.forEach((rFunction, typeAdapter) -> typeAdapter.serializer().accept(buffer, rFunction.apply(instance)));
    }

    private static <R extends Record> Object getRecordEntry(R instance, Method accessor) {
        try {
            return accessor.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to get message contents", e);
        }
    }

    public static <T> void registerTypeAdapter(Class<T> clazz, BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {
        if (TYPE_ADAPTERS.containsKey(clazz)) throw new IllegalStateException("Class '" + clazz.getName() + "' already has a type adapter");
        TYPE_ADAPTERS.put(clazz, new TypeAdapter<>(serializer, deserializer));
    }

    private static <T> TypeAdapter<T> createAdapter(Class<T> componentClass, RecordComponent component) {
        if (Map.class.isAssignableFrom(componentClass)) {
            var typeAnnotation = component.getAnnotation(MapTypes.class);
            return (TypeAdapter<T>) getMapAdapter(conform(componentClass, Map.class), typeAnnotation.keys(), typeAnnotation.values());
        }

        if (Collection.class.isAssignableFrom(componentClass)) {
            var typeAnnotation = component.getAnnotation(CollectionType.class);
            return (TypeAdapter<T>) getCollectionAdapter(conform(componentClass, Collection.class), typeAnnotation.value());
        }

        return getTypeAdapter(componentClass);
    }

    public static <T> TypeAdapter<T> getTypeAdapter(Class<T> clazz) {
        if (!TYPE_ADAPTERS.containsKey(clazz)) {
            throw new IllegalStateException(clazz.isPrimitive() ?
                    "Primitive type '" + clazz.getName() + "' can not be serialized. Use the boxed type instead" :
                    "No type adapter available for class '" + clazz.getName() + "'");
        }

        return (TypeAdapter<T>) TYPE_ADAPTERS.get(clazz);
    }

    public static <K, V, T extends Map<K, V>> TypeAdapter<T> getMapAdapter(Class<T> clazz, Class<K> keyClass, Class<V> valueClass) {
        var keyAdapter = getTypeAdapter(keyClass);
        var valueAdapter = getTypeAdapter(valueClass);
        return new TypeAdapter<>((buf, t) -> buf.writeMap(t, keyAdapter.serializer(), valueAdapter.serializer()),
                buf -> buf.readMap(buf1 -> (T) new HashMap<>(), keyAdapter.deserializer(), valueAdapter.deserializer()));
    }

    public static <E, T extends Collection<E>> TypeAdapter<T> getCollectionAdapter(Class<T> clazz, Class<E> elementClass) {
        var elementAdapter = getTypeAdapter(elementClass);
        return new TypeAdapter<>((buf, t) -> buf.writeCollection(t, elementAdapter.serializer()),
                buf -> buf.readCollection(value -> (T) new ArrayList<>(), elementAdapter.deserializer()));
    }

    private static <T> Class<T> conform(Class<?> clazz, Class<T> target) {
        return (Class<T>) clazz;
    }

    static {
        registerTypeAdapter(Boolean.class, PacketByteBuf::writeBoolean, PacketByteBuf::readBoolean);
        registerTypeAdapter(Double.class, PacketByteBuf::writeDouble, PacketByteBuf::readDouble);
        registerTypeAdapter(Float.class, PacketByteBuf::writeFloat, PacketByteBuf::readFloat);

        registerTypeAdapter(Byte.class, (BiConsumer<PacketByteBuf, Byte>) PacketByteBuf::writeByte, PacketByteBuf::readByte);
        registerTypeAdapter(Short.class, (BiConsumer<PacketByteBuf, Short>) PacketByteBuf::writeShort, PacketByteBuf::readShort);
        registerTypeAdapter(Integer.class, PacketByteBuf::writeVarInt, PacketByteBuf::readVarInt);
        registerTypeAdapter(Long.class, PacketByteBuf::writeLong, PacketByteBuf::readLong);

        registerTypeAdapter(String.class, PacketByteBuf::writeString, PacketByteBuf::readString);
        registerTypeAdapter(BlockPos.class, PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);
        registerTypeAdapter(ItemStack.class, PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack);
        registerTypeAdapter(Identifier.class, PacketByteBuf::writeIdentifier, PacketByteBuf::readIdentifier);

        registerTypeAdapter(Vec3d.class, (buf, vec3d) -> VectorSerializer.write(vec3d, buf), VectorSerializer::read);
        registerTypeAdapter(Vec3f.class, (buf, vec3d) -> VectorSerializer.writef(vec3d, buf), VectorSerializer::readf);
    }

}

