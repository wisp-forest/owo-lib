
package io.wispforest.owo.network.serialization;

import io.wispforest.owo.util.VectorSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple wrapper for (de-)serialization methods on {@link PacketByteBuf}s. For
 * collection types like Maps and Lists, providers must be registered via
 * {@link #registerCollectionProvider(Class, Supplier)} if types other
 * than {@link Collection}, {@link List} and {@link Map} are desired
 *
 * @param <T> The type of object this adapter can handle
 */
public record TypeAdapter<T>(BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {

    private static final Map<Class<?>, Supplier<?>> COLLECTION_PROVIDERS = new HashMap<>();
    private static final Map<Class<?>, TypeAdapter<?>> TYPE_ADAPTERS = new HashMap<>();

    /**
     * Enables (de-)serialization for the given class
     *
     * @param clazz        The object class to serialize
     * @param serializer   The serialization method
     * @param deserializer The deserialization method
     * @param <T>          The type of object to register an adapter for
     */
    public static <T> void register(Class<T> clazz, BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {
        if (TYPE_ADAPTERS.containsKey(clazz)) throw new IllegalStateException("Class '" + clazz.getName() + "' already has a type adapter");
        TYPE_ADAPTERS.put(clazz, new TypeAdapter<>(serializer, deserializer));
    }

    @SafeVarargs
    private static <T> void register(BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer, Class<T>... classes) {
        final var adapter = new TypeAdapter<T>(serializer, deserializer);
        for (var clazz : classes) {
            if (TYPE_ADAPTERS.containsKey(clazz)) throw new IllegalStateException("Class '" + clazz + "' already has a type adapter");
            TYPE_ADAPTERS.put(clazz, adapter);
        }
    }

    /**
     * Gets the type adapter for the given class, or throws
     * an exception if none is registered
     *
     * @param clazz The class to obtain an adapter for
     * @return The respective type adapter instance
     */
    public static <T> TypeAdapter<T> get(Class<T> clazz) {
        if (!TYPE_ADAPTERS.containsKey(clazz)) {
            throw new IllegalStateException("No type adapter available for class '" + clazz.getName() + "'");
        }

        //noinspection unchecked
        return (TypeAdapter<T>) TYPE_ADAPTERS.get(clazz);
    }

    /**
     * Tries to get the type adapter for the given class
     *
     * @param clazz The class to obtain an adapter for
     * @return An empty optional if no adapter is registered
     */
    public static <T> Optional<TypeAdapter<T>> maybeGet(Class<T> clazz) {
        //noinspection unchecked
        return Optional.ofNullable((TypeAdapter<T>) TYPE_ADAPTERS.get(clazz));
    }

    /**
     * Registers a supplier that creates empty collections for the
     * map and collection adapters to use
     *
     * @param clazz    The container class to register a provider for
     * @param provider A provider that creates some default type for the given
     *                 class
     */
    public static <T> void registerCollectionProvider(Class<T> clazz, Supplier<T> provider) {
        if (COLLECTION_PROVIDERS.containsKey(clazz)) throw new IllegalStateException("Collection class '" + clazz.getName() + "' already has a provider");
        COLLECTION_PROVIDERS.put(clazz, provider);
    }

    /**
     * Creates a new collection instance
     * for the given container class
     *
     * @param clazz The container class
     * @return The created collection
     */
    public static <T> T createCollection(Class<? extends T> clazz) {
        if (!COLLECTION_PROVIDERS.containsKey(clazz)) {
            throw new IllegalStateException("No collection provider registered for collection class " + clazz.getName());
        }

        //noinspection unchecked
        return ((Supplier<T>) COLLECTION_PROVIDERS.get(clazz)).get();
    }

    /**
     * Tries to create an adapter capable of
     * serializing the given map type
     *
     * @param clazz      The map type
     * @param keyClass   The type of the map's keys
     * @param valueClass The type of the map's values
     * @return The created adapter
     */
    public static <K, V, T extends Map<K, V>> TypeAdapter<T> createMapAdapter(Class<T> clazz, Class<K> keyClass, Class<V> valueClass) {
        createCollection(clazz);

        var keyAdapter = get(keyClass);
        var valueAdapter = get(valueClass);
        return new TypeAdapter<>((buf, t) -> buf.writeMap(t, keyAdapter.serializer(), valueAdapter.serializer()),
                buf -> buf.readMap(buf1 -> createCollection(clazz), keyAdapter.deserializer(), valueAdapter.deserializer()));
    }

    /**
     * Tries to create an adapter capable of
     * serializing the given collection type
     *
     * @param clazz        The collection type
     * @param elementClass The type of the collections elements
     * @return The created adapter
     */
    public static <E, T extends Collection<E>> TypeAdapter<T> createCollectionAdapter(Class<T> clazz, Class<E> elementClass) {
        createCollection(clazz);

        var elementAdapter = get(elementClass);
        return new TypeAdapter<>((buf, t) -> buf.writeCollection(t, elementAdapter.serializer()),
                buf -> buf.readCollection(value -> createCollection(clazz), elementAdapter.deserializer()));
    }

    /**
     * Tries to create an adapter capable of
     * serializing arrays of the given element type
     *
     * @param elementClass The array element type
     * @return The created adapter
     */
    @SuppressWarnings("unchecked")
    public static <E> TypeAdapter<E[]> createArrayAdapter(Class<E> elementClass) {
        var elementAdapter = get(elementClass);
        return new TypeAdapter<>((buf, t) -> {
            final int length = Array.getLength(t);
            buf.writeVarInt(length);
            for (int i = 0; i < length; i++) {
                elementAdapter.serializer().accept(buf, (E) Array.get(t, i));
            }
        }, buf -> {
            final int length = buf.readVarInt();
            Object array = Array.newInstance(elementClass, length);
            for (int i = 0; i < length; i++) {
                Array.set(array, i, elementAdapter.deserializer().apply(buf));
            }
            return (E[]) array;
        });
    }

    /**
     * Tries to create an adapter capable of
     * serializing the given record class
     *
     * @param clazz The class to create an adapter for
     * @return The created adapter
     */
    public static <R extends Record> TypeAdapter<R> createRecordAdapter(Class<R> clazz) {
        var serializer = RecordSerializer.create(clazz);
        return new TypeAdapter<>(serializer::write, serializer::read);
    }

    /**
     * Tries to create an adapter capable of serializing
     * the given enum type
     *
     * @param enumClass The type of enum to create an adapter for
     * @return The created adapter
     */
    public static <E extends Enum<E>> TypeAdapter<E> createEnumAdapter(Class<E> enumClass) {
        return new TypeAdapter<>(PacketByteBuf::writeEnumConstant, buf -> buf.readEnumConstant(enumClass));
    }

    static {

        // ----------
        // Primitives
        // ----------

        register(PacketByteBuf::writeBoolean, PacketByteBuf::readBoolean, Boolean.class, boolean.class);
        register(PacketByteBuf::writeVarInt, PacketByteBuf::readVarInt, Integer.class, int.class);
        register(PacketByteBuf::writeVarLong, PacketByteBuf::readVarLong, Long.class, long.class);
        register(PacketByteBuf::writeFloat, PacketByteBuf::readFloat, Float.class, float.class);
        register(PacketByteBuf::writeDouble, PacketByteBuf::readDouble, Double.class, double.class);

        register((BiConsumer<PacketByteBuf, Byte>) PacketByteBuf::writeByte, PacketByteBuf::readByte, Byte.class, byte.class);
        register((BiConsumer<PacketByteBuf, Short>) PacketByteBuf::writeShort, PacketByteBuf::readShort, Short.class, short.class);
        register((BiConsumer<PacketByteBuf, Character>) PacketByteBuf::writeChar, PacketByteBuf::readChar, Character.class, char.class);

        // ----
        // Misc
        // ----

        register(String.class, PacketByteBuf::writeString, PacketByteBuf::readString);
        register(UUID.class, PacketByteBuf::writeUuid, PacketByteBuf::readUuid);
        register(Date.class, PacketByteBuf::writeDate, PacketByteBuf::readDate);

        // --------
        // MC Types
        // --------

        register(BlockPos.class, PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);
        register(ItemStack.class, PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack);
        register(Identifier.class, PacketByteBuf::writeIdentifier, PacketByteBuf::readIdentifier);
        register(NbtCompound.class, PacketByteBuf::writeNbt, PacketByteBuf::readNbt);
        register(BlockHitResult.class, PacketByteBuf::writeBlockHitResult, PacketByteBuf::readBlockHitResult);
        register(BitSet.class, PacketByteBuf::writeBitSet, PacketByteBuf::readBitSet);
        register(Text.class, PacketByteBuf::writeText, PacketByteBuf::readText);

        register(Vec3d.class, (buf, vec3d) -> VectorSerializer.write(vec3d, buf), VectorSerializer::read);
        register(Vec3f.class, (buf, vec3d) -> VectorSerializer.writef(vec3d, buf), VectorSerializer::readf);

        // -----------
        // Collections
        // -----------

        registerCollectionProvider(Collection.class, HashSet::new);
        registerCollectionProvider(List.class, ArrayList::new);
        registerCollectionProvider(Map.class, HashMap::new);
    }

}
