package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.network.serialization.SealedPolymorphic;
import io.wispforest.owo.serialization.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReflectionCodeckBuilder {

    private static final Map<Class<?>, Supplier<?>> COLLECTION_PROVIDERS = new HashMap<>();
    private static final Map<Class<?>, Codeck<?>> SERIALIZERS = new HashMap<>();

    /**
     * Enables (de-)serialization for the given class
     *
     * @param clazz      The object class to serialize
     * @param serializer The serializer
     * @param <T>        The type of object to register a serializer for
     */
    public static <T> void register(Class<T> clazz, Codeck<T> serializer) {
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
    public static <T> void register(Class<T> clazz, BiConsumer<Serializer, T> serializer, Function<Deserializer, T> deserializer) {
        register(clazz, Codeck.of(serializer, deserializer));
    }

    @SafeVarargs
    private static <T> void register(Codeck<T> kodeck, Class<T>... classes) {
        for (var clazz : classes) register(clazz, kodeck);
    }

    @SafeVarargs
    private static <T> void register(BiConsumer<Serializer, T> serializer, Function<Deserializer, T> deserializer, Class<T>... classes) {
        final var kodeck = Codeck.of(serializer, deserializer);

        for (var clazz : classes) register(clazz, kodeck);
    }

    /**
     * Gets the serializer for the given class, using additional data from
     * generics, or throws an exception if none is registered
     *
     * @param type The type to obtain a serializer for
     * @return The respective serializer instance
     */
    @SuppressWarnings("unchecked")
    public static Codeck<?> getGeneric(Type type) {
        if (type instanceof Class<?> klass) return get(klass);

        var pType = (ParameterizedType) type;
        Class<?> raw = (Class<?>) pType.getRawType();
        var typeArgs = pType.getActualTypeArguments();

        if (Map.class.isAssignableFrom(raw)) {
            return ReflectionCodeckBuilder.createMapSerializer(conform(raw, Map.class), (Class<?>) typeArgs[0], (Class<?>) typeArgs[1]);
        }

        if (Collection.class.isAssignableFrom(raw)) {
            return ReflectionCodeckBuilder.createCollectionSerializer(conform(raw, Collection.class), (Class<?>) typeArgs[0]);
        }

        if (Optional.class.isAssignableFrom(raw)) {
            return ReflectionCodeckBuilder.createOptionalSerializer((Class<?>) typeArgs[0]);
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
    public static <T> Codeck<T> get(Class<T> clazz) {
        Codeck<T> serializer = getOrNull(clazz);

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
    public static <T> Optional<Codeck<T>> maybeGet(Class<T> clazz) {
        return Optional.ofNullable(getOrNull(clazz));
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable Codeck<T> getOrNull(Class<T> clazz) {
        Codeck<T> serializer = (Codeck<T>) SERIALIZERS.get(clazz);

        if (serializer == null) {
            if (Record.class.isAssignableFrom(clazz))
                serializer = (Codeck<T>) ReflectionCodeckBuilder.createRecordSerializer(conform(clazz, Record.class));
            else if (clazz.isEnum())
                serializer = (Codeck<T>) ReflectionCodeckBuilder.createEnumSerializer(conform(clazz, Enum.class));
            else if (clazz.isArray())
                serializer = (Codeck<T>) ReflectionCodeckBuilder.createArraySerializer(clazz.getComponentType());
            else if (clazz.isAnnotationPresent(SealedPolymorphic.class))
                serializer = (Codeck<T>) ReflectionCodeckBuilder.createSealedSerializer(clazz);
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
    public static <K, V, T extends Map<K, V>> Codeck<T> createMapSerializer(Class<T> clazz, Class<K> keyClass, Class<V> valueClass) {
        createCollection(clazz);

        var keyCodeck = get(keyClass);
        var valueCodeck = get(valueClass);

        return keyCodeck == Codeck.STRING
                ? (Codeck<T>) valueCodeck.map()
                : (Codeck<T>) Codeck.mapOf(keyCodeck, valueCodeck);
    }

    /**
     * Tries to create a serializer capable of
     * serializing the given collection type
     *
     * @param clazz        The collection type
     * @param elementClass The type of the collections elements
     * @return The created serializer
     */
    public static <E, T extends Collection<E>> Codeck<T> createCollectionSerializer(Class<T> clazz, Class<E> elementClass) {
        createCollection(clazz);

        var elementCodeck = get(elementClass);

        return elementCodeck.list()
                .then(es -> {
                    T collection = createCollection(clazz);

                    collection.addAll(es);

                    return collection;
                }, List::copyOf);
    }

    /**
     * Tries to create a serializer capable of
     * serializing optionals with the given element type
     *
     * @param elementClass The type of the collections elements
     * @return The created serializer
     */
    public static <E> Codeck<Optional<E>> createOptionalSerializer(Class<E> elementClass) {
        var elementCodeck = get(elementClass);

        return elementCodeck.ofOptional();
    }

    /**
     * Tries to create a serializer capable of
     * serializing arrays of the given element type
     *
     * @param elementClass The array element type
     * @return The created serializer
     */
    @SuppressWarnings("unchecked")
    public static Codeck<?> createArraySerializer(Class<?> elementClass) {
        var elementSerializer = (Codeck<Object>) get(elementClass);

        return elementSerializer.list().then(list -> {
            final int length = list.size();
            Object array = Array.newInstance(elementClass, length);
            for (int i = 0; i < length; i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }, t -> {
            final int length = Array.getLength(t);
            List<Object> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(Array.get(t, i));
            }
            return list;
        });
    }

    /**
     * Tries to create a serializer capable of
     * serializing the given record class
     *
     * @param clazz The class to create a serializer for
     * @return The created serializer
     */
    public static <R extends Record> Codeck<R> createRecordSerializer(Class<R> clazz) {
        return RecordCodeck.create(clazz);
    }

    /**
     * Tries to create a serializer capable of serializing
     * the given enum type
     *
     * @param enumClass The type of enum to create a serializer for
     * @return The created serializer
     */
    public static <E extends Enum<E>> Codeck<E> createEnumSerializer(Class<E> enumClass) {
        return Codeck.VAR_INT.then(i -> enumClass.getEnumConstants()[i], Enum::ordinal);
    }

    @SuppressWarnings("unchecked")
    public static <T, K> Codeck<T> createDispatchedSerializer(Function<K, Codeck<? extends T>> keyToCodeck, Function<T, K> keyGetter, Codeck<K> keyCodeck) {
        return Codeck.dispatchedOf(keyToCodeck, keyGetter, keyCodeck);
    }

    @SuppressWarnings("unchecked")
    private static Codeck<?> createSealedSerializer(Class<?> commonClass) {
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

        Int2ObjectMap<Codeck<?>> serializerMap = new Int2ObjectOpenHashMap<>();
        Reference2IntMap<Class<?>> classesMap = new Reference2IntOpenHashMap<>();

        classesMap.defaultReturnValue(-1);

        for (int i = 0; i < sortedPermittedSubclasses.size(); i++) {
            Class<?> klass = sortedPermittedSubclasses.get(i);

            serializerMap.put(i, ReflectionCodeckBuilder.get(klass));
            classesMap.put(klass, i);
        }

        return Codeck.dispatchedOf(serializerMap::get, v -> classesMap.getInt(v.getClass()), Codeck.INT);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> conform(Class<?> clazz, Class<T> target) {
        return (Class<T>) clazz;
    }

    static {

        // ----------
        // Primitives
        // ----------

        register(Codeck.BOOLEAN, Boolean.class, boolean.class);
        register(Codeck.INT, Integer.class, int.class);
        register(Codeck.LONG, Long.class, long.class);
        register(Codeck.FLOAT, Float.class, float.class);
        register(Codeck.DOUBLE, Double.class, double.class);

        register(Codeck.BYTE, Byte.class, byte.class);
        register(Codeck.SHORT, Short.class, short.class);
        register(Codeck.SHORT.then(aShort -> (char) aShort.shortValue(), character -> (short) character.charValue()), Character.class, char.class);

        register(Void.class, Codeck.EMPTY);

        // ----
        // Misc
        // ----

        register(String.class, Codeck.STRING);
        register(UUID.class, Codeck.UUID);
        register(Date.class, Codeck.DATE);
        register(PacketByteBuf.class, Codeck.PACKET_BYTE_BUF);

        // --------
        // MC Types
        // --------

        register(BlockPos.class, Codeck.BLOCK_POS);
        register(ChunkPos.class, Codeck.CHUNK_POS);
        register(ItemStack.class, Codeck.ITEM_STACK);
        register(Identifier.class, Codeck.IDENTIFIER);
        register(NbtCompound.class, Codeck.COMPOUND);
        register(
                BlockHitResult.class,
                new StructCodeck<>(){
                    final Codeck<Direction> DIRECTION = createEnumSerializer(Direction.class);

                    @Override
                    public void encode(StructSerializer serializer, BlockHitResult hitResult) {
                        BlockPos blockPos = hitResult.getBlockPos();
                        serializer.field("blockPos", Codeck.BLOCK_POS, blockPos)
                                .field("side", DIRECTION, hitResult.getSide());

                        Vec3d vec3d = hitResult.getPos();
                        serializer.field("x", Codeck.FLOAT, (float)(vec3d.x - (double)blockPos.getX()))
                                .field("y", Codeck.FLOAT, (float)(vec3d.x - (double)blockPos.getX()))
                                .field("z", Codeck.FLOAT, (float)(vec3d.x - (double)blockPos.getX()))
                                .field("inside", Codeck.BOOLEAN, hitResult.isInsideBlock());
                    }

                    @Override
                    public BlockHitResult decode(StructDeserializer deserializer) {
                        BlockPos blockPos = deserializer.field("blockPos", Codeck.BLOCK_POS);
                        Direction direction = deserializer.field("side", DIRECTION);

                        float f = deserializer.field("x", Codeck.FLOAT);
                        float g = deserializer.field("y", Codeck.FLOAT);
                        float h = deserializer.field("z", Codeck.FLOAT);

                        boolean bl = deserializer.field("inside", Codeck.BOOLEAN);
                        return new BlockHitResult(
                                new Vec3d((double)blockPos.getX() + (double)f, (double)blockPos.getY() + (double)g, (double)blockPos.getZ() + (double)h), direction, blockPos, bl
                        );
                    }
                }
        );
        register(BitSet.class, Codeck.BITSET);
        register(Text.class, Codeck.TEXT);

        register(ParticleEffect.class,
                Codeck.PACKET_BYTE_BUF.then(
                        byteBuf -> {
                            //noinspection rawtypes
                            final ParticleType particleType = Registries.PARTICLE_TYPE.get(byteBuf.readInt());
                            //noinspection unchecked, ConstantConditions

                            return particleType.getParametersFactory().read(particleType, byteBuf);
                        },
                        particleEffect -> {
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeInt(Registries.PARTICLE_TYPE.getRawId(particleEffect.getType()));
                            particleEffect.write(buf);

                            return buf;
                        }
                )
        );

        register(Vec3d.class, Codeck.DOUBLE.list()
                .then(
                        doubles -> new Vec3d(doubles.get(0), doubles.get(1), doubles.get(2)),
                        vec3d -> List.of(vec3d.getX(), vec3d.getY(), vec3d.getZ())
                ));

        register(Vector3f.class, Codeck.FLOAT.list()
                .then(
                        doubles -> new Vector3f(doubles.get(0), doubles.get(1), doubles.get(2)),
                        vec3d -> List.of(vec3d.x(), vec3d.y(), vec3d.z())
                ));

        // -----------
        // Collections
        // -----------

        registerCollectionProvider(Collection.class, HashSet::new);
        registerCollectionProvider(List.class, ArrayList::new);
        registerCollectionProvider(Map.class, LinkedHashMap::new);
    }
}
