package io.wispforest.owo.network.serialization;

import io.wispforest.owo.network.annotations.SealedPolymorphic;
import io.wispforest.owo.util.VectorSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A simple wrapper for (de-)serialization methods on {@link PacketByteBuf}s. For
 * collection types like Maps and Lists, providers must be registered via
 * {@link #registerCollectionProvider(Class, Supplier)} if types other
 * than {@link Collection}, {@link List} and {@link Map} are desired
 *
 * @param <T> The type of object this serializer can handle
 */
public record PacketBufSerializer<T>(BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {

    private static final Map<Class<?>, Supplier<?>> COLLECTION_PROVIDERS = new HashMap<>();
    private static final Map<Class<?>, PacketBufSerializer<?>> SERIALIZERS = new HashMap<>();

    /**
     * Enables (de-)serialization for the given class
     *
     * @param clazz        The object class to serialize
     * @param serializer   The serializer
     * @param <T>          The type of object to register a serializer for
     */
    public static <T> void register(Class<T> clazz, PacketBufSerializer<T> serializer) {
        if (SERIALIZERS.containsKey(clazz)) throw new IllegalStateException("Class '" + clazz.getName() + "' already has a serializer");
        SERIALIZERS.put(clazz, serializer);
    }

    /**
     * Enables (de-)serialization for the given class
     *
     * @param clazz        The object class to serialize
     * @param serializer   The serialization method
     * @param deserializer The deserialization method
     * @param <T>          The type of object to register a serializer for
     */
    public static <T> void register(Class<T> clazz, BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {
        register(clazz, new PacketBufSerializer<>(serializer, deserializer));
    }

    @SafeVarargs
    private static <T> void register(BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer, Class<T>... classes) {
        final var packetSerializer = new PacketBufSerializer<T>(serializer, deserializer);
        for (var clazz : classes) {
            register(clazz, packetSerializer);
        }
    }

    /**
     * Gets the serializer for the given class, using additional data from
     * generics, or throws an exception if none is registered
     *
     * @param type The type to obtain a serializer for
     * @return The respective serializer instance
     */
    @SuppressWarnings("unchecked")
    public static PacketBufSerializer<?> getGeneric(Type type) {
        if (type instanceof Class<?> klass) return get(klass);

        var pType = (ParameterizedType) type;
        Class<?> raw = (Class<?>) pType.getRawType();
        var typeArgs = pType.getActualTypeArguments();

        if (Map.class.isAssignableFrom(raw)) {
            return PacketBufSerializer.createMapSerializer(conform(raw, Map.class), (Class<?>) typeArgs[0], (Class<?>) typeArgs[1]);
        }

        if (Collection.class.isAssignableFrom(raw)) {
            return PacketBufSerializer.createCollectionSerializer(conform(raw, Collection.class), (Class<?>) typeArgs[0]);
        }

        if (Optional.class.isAssignableFrom(raw)) {
            return PacketBufSerializer.createOptionalSerializer((Class<?>) typeArgs[0]);
        }

        return get(raw);
    }

    /**
     * Gets the serializer for the given class, or throws
     * an exception if none is registered
     *
     * @param clazz The class to obtain a serializer for
     * @return The respective serializer instance
     */
    public static <T> PacketBufSerializer<T> get(Class<T> clazz) {
        PacketBufSerializer<T> serializer = getOrNull(clazz);

        if (serializer == null) {
            throw new IllegalStateException("No serializer available for class '" + clazz.getName() + "'");
        }

        return serializer;
    }

    /**
     * Tries to get the serializer for the given class
     *
     * @param clazz The class to obtain a serializer for
     * @return An empty optional if no serializer is registered
     */
    public static <T> Optional<PacketBufSerializer<T>> maybeGet(Class<T> clazz) {
        return Optional.ofNullable(getOrNull(clazz));
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable PacketBufSerializer<T> getOrNull(Class<T> clazz) {
        PacketBufSerializer<T> serializer = (PacketBufSerializer<T>) SERIALIZERS.get(clazz);

        if (serializer == null) {
            if (Record.class.isAssignableFrom(clazz))
                serializer = (PacketBufSerializer<T>) PacketBufSerializer.createRecordSerializer(conform(clazz, Record.class));
            else if (clazz.isEnum())
                serializer = (PacketBufSerializer<T>) PacketBufSerializer.createEnumSerializer(conform(clazz, Enum.class));
            else if (clazz.isArray())
                serializer = (PacketBufSerializer<T>) PacketBufSerializer.createArraySerializer(clazz.getComponentType());
            else if (clazz.isAnnotationPresent(SealedPolymorphic.class))
                serializer = (PacketBufSerializer<T>) PacketBufSerializer.createSealedSerializer(clazz);
            else
                return null;

            SERIALIZERS.put(clazz, serializer);
        }


        return serializer;
    }

    /**
     * Registers a supplier that creates empty collections for the
     * map and collection serializers to use
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
     * Tries to create a serializer capable of
     * serializing the given map type
     *
     * @param clazz      The map type
     * @param keyClass   The type of the map's keys
     * @param valueClass The type of the map's values
     * @return The created serializer
     */
    public static <K, V, T extends Map<K, V>> PacketBufSerializer<T> createMapSerializer(Class<T> clazz, Class<K> keyClass, Class<V> valueClass) {
        createCollection(clazz);

        var keySerializer = get(keyClass);
        var valueSerializer = get(valueClass);
        return new PacketBufSerializer<>((buf, t) -> buf.writeMap(t, keySerializer.serializer(), valueSerializer.serializer()),
                buf -> buf.readMap(buf1 -> createCollection(clazz), keySerializer.deserializer(), valueSerializer.deserializer()));
    }

    /**
     * Tries to create a serializer capable of
     * serializing the given collection type
     *
     * @param clazz        The collection type
     * @param elementClass The type of the collections elements
     * @return The created serializer
     */
    public static <E, T extends Collection<E>> PacketBufSerializer<T> createCollectionSerializer(Class<T> clazz, Class<E> elementClass) {
        createCollection(clazz);

        var elementSerializer = get(elementClass);
        return new PacketBufSerializer<>((buf, t) -> buf.writeCollection(t, elementSerializer.serializer()),
                buf -> buf.readCollection(value -> createCollection(clazz), elementSerializer.deserializer()));
    }

    /**
     * Tries to create a serializer capable of
     * serializing optionals with the given element type
     *
     * @param elementClass The type of the collections elements
     * @return The created serializer
     */
    public static <E> PacketBufSerializer<Optional<E>> createOptionalSerializer(Class<E> elementClass) {
        var elementSerializer = get(elementClass);
        return new PacketBufSerializer<>((buf, t) -> buf.writeOptional(t, elementSerializer.serializer()),
                buf -> buf.readOptional(elementSerializer.deserializer()));
    }

    /**
     * Tries to create a serializer capable of
     * serializing arrays of the given element type
     *
     * @param elementClass The array element type
     * @return The created serializer
     */
    @SuppressWarnings("unchecked")
    public static PacketBufSerializer<?> createArraySerializer(Class<?> elementClass) {
        var elementSerializer = (PacketBufSerializer<Object>) get(elementClass);
        return new PacketBufSerializer<>((buf, t) -> {
            final int length = Array.getLength(t);
            buf.writeVarInt(length);
            for (int i = 0; i < length; i++) {
                elementSerializer.serializer().accept(buf, Array.get(t, i));
            }
        }, buf -> {
            final int length = buf.readVarInt();
            Object array = Array.newInstance(elementClass, length);
            for (int i = 0; i < length; i++) {
                Array.set(array, i, elementSerializer.deserializer().apply(buf));
            }
            return array;
        });
    }

    /**
     * Tries to create a serializer capable of
     * serializing the given record class
     *
     * @param clazz The class to create a serializer for
     * @return The created serializer
     */
    public static <R extends Record> PacketBufSerializer<R> createRecordSerializer(Class<R> clazz) {
        var serializer = RecordSerializer.create(clazz);
        return new PacketBufSerializer<>(serializer::write, serializer::read);
    }

    /**
     * Tries to create a serializer capable of serializing
     * the given enum type
     *
     * @param enumClass The type of enum to create a serializer for
     * @return The created serializer
     */
    public static <E extends Enum<E>> PacketBufSerializer<E> createEnumSerializer(Class<E> enumClass) {
        return new PacketBufSerializer<>(PacketByteBuf::writeEnumConstant, buf -> buf.readEnumConstant(enumClass));
    }

    @SuppressWarnings("unchecked")
    private static PacketBufSerializer<?> createSealedSerializer(Class<?> commonClass) {
        if (!commonClass.isSealed())
            throw new IllegalStateException("@SealedPolymorphic class should be sealed!");

        List<Class<?>> sortedPermittedSubclasses = Arrays.stream(commonClass.getPermittedSubclasses()).collect(Collectors.toList());

        for (int i = 0; i < sortedPermittedSubclasses.size(); i++) {
            Class<?> klass = sortedPermittedSubclasses.get(i);

            if (klass.isSealed()) {
                for (Class<?> subclass : klass.getPermittedSubclasses()) {
                    if (!sortedPermittedSubclasses.contains(subclass))
                        sortedPermittedSubclasses.add(subclass);
                }
            }
        }

        for (Class<?> klass : sortedPermittedSubclasses) {
            if (!klass.isSealed() && !Modifier.isFinal(klass.getModifiers()))
                throw new IllegalStateException("Subclasses of a @SealedPolymorphic class must be sealed themselves!");
        }

        sortedPermittedSubclasses.sort(Comparator.comparing(Class::getName));

        Int2ObjectMap<PacketBufSerializer<?>> serializerMap = new Int2ObjectOpenHashMap<>();
        Reference2IntMap<Class<?>> classesMap = new Reference2IntOpenHashMap<>();

        classesMap.defaultReturnValue(-1);

        for (int i = 0; i < sortedPermittedSubclasses.size(); i++) {
            Class<?> klass = sortedPermittedSubclasses.get(i);

            serializerMap.put(i, PacketBufSerializer.get(klass));
            classesMap.put(klass, i);
        }
        
        return new PacketBufSerializer<>((buf, value) -> {
            int idx = classesMap.getInt(value.getClass());

            if (idx == -1) throw new IllegalStateException("Tried to serialize instance of " + value.getClass() + " as a " + commonClass);

            buf.writeVarInt(idx);
            ((BiConsumer<PacketByteBuf, Object>) serializerMap.get(idx).serializer).accept(buf, value);
        }, buf -> {
            int idx = buf.readVarInt();
            PacketBufSerializer<Object> serializer = (PacketBufSerializer<Object>) serializerMap.get(idx);

            if (serializer == null) throw new IllegalStateException("Unknown index " + idx);

            return serializer.deserializer.apply(buf);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> conform(Class<?> clazz, Class<T> target) {
        return (Class<T>) clazz;
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

        register(Void.class, (packetByteBuf, unused) -> {}, packetByteBuf -> null);

        // ----
        // Misc
        // ----

        register(String.class, PacketByteBuf::writeString, PacketByteBuf::readString);
        register(UUID.class, PacketByteBuf::writeUuid, PacketByteBuf::readUuid);
        register(Date.class, PacketByteBuf::writeDate, PacketByteBuf::readDate);
        register(PacketByteBuf.class, (buf, other) -> {
            buf.writeVarInt(other.readableBytes());
            buf.writeBytes(other);
        }, buf -> new PacketByteBuf(buf.readBytes(buf.readVarInt())));

        // --------
        // MC Types
        // --------

        register(BlockPos.class, PacketByteBuf::writeBlockPos, PacketByteBuf::readBlockPos);
        register(ChunkPos.class, PacketByteBuf::writeChunkPos, PacketByteBuf::readChunkPos);
        register(ItemStack.class, PacketByteBuf::writeItemStack, PacketByteBuf::readItemStack);
        register(Identifier.class, PacketByteBuf::writeIdentifier, PacketByteBuf::readIdentifier);
        register(NbtCompound.class, PacketByteBuf::writeNbt, PacketByteBuf::readNbt);
        register(BlockHitResult.class, PacketByteBuf::writeBlockHitResult, PacketByteBuf::readBlockHitResult);
        register(BitSet.class, PacketByteBuf::writeBitSet, PacketByteBuf::readBitSet);
        register(Text.class, PacketByteBuf::writeText, PacketByteBuf::readText);

        PacketBufSerializer.register(ParticleEffect.class, (buf, particleEffect) -> {
            buf.writeInt(Registry.PARTICLE_TYPE.getRawId(particleEffect.getType()));
            particleEffect.write(buf);
        }, buf -> {
            //noinspection rawtypes
            final ParticleType particleType = Registry.PARTICLE_TYPE.get(buf.readInt());
            //noinspection unchecked, ConstantConditions
            return particleType.getParametersFactory().read(particleType, buf);
        });

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
