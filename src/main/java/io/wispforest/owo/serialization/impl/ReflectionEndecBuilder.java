package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.network.serialization.SealedPolymorphic;
import io.wispforest.owo.serialization.BuiltInEndecs;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.impl.nbt.NbtEndec;
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
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReflectionEndecBuilder {

    private static final Map<Class<?>, Endec<?>> SERIALIZERS = new HashMap<>();

    /**
     * Enables (de-)serialization for the given class
     *
     * @param clazz      The object class to serialize
     * @param serializer The serializer
     * @param <T>        The type of object to register a serializer for
     */
    public static <T> void register(Class<T> clazz, Endec<T> serializer) {
        if (SERIALIZERS.containsKey(clazz)) {
            throw new IllegalStateException("Class '" + clazz.getName() + "' already has a serializer");
        }
        SERIALIZERS.put(clazz, serializer);
    }

    /**
     * Enables (de-)serialization for the given class
     *
     * @param clazz        The object class to serialize
     * @param endec        The endec for the given type
     * @param <T>          The type of object to register a serializer for
     */
    @SuppressWarnings("rawtypes")
    public static <T> void register(Endec<T> endec, Class<T> clazz) {
        register(clazz, endec);
    }

    @SafeVarargs
    private static <T> void register(Endec<T> endec, Class<T>... classes) {
        for (var clazz : classes) register(clazz, endec);
    }

    @SafeVarargs
    @SuppressWarnings("rawtypes")
    private static <T> void register(BiConsumer<Serializer<?>, T> serializer, Function<Deserializer<?>, T> deserializer, Class<T>... classes) {
        final var kodeck = Endec.of(serializer, deserializer);

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
    public static Endec<?> getGeneric(Type type) {
        if (type instanceof Class<?> klass) return get(klass);

        var pType = (ParameterizedType) type;
        Class<?> raw = (Class<?>) pType.getRawType();
        var typeArgs = pType.getActualTypeArguments();

        if (Map.class.isAssignableFrom(raw)) {
            return (typeArgs[0] instanceof ParameterizedType)
                    ? Endec.map(getGeneric(typeArgs[0]), getGeneric(typeArgs[1]))
                    : getGeneric(typeArgs[1]).mapOf();
        }

        if (List.class.isAssignableFrom(raw)) {
            return getGeneric(typeArgs[0]).listOf();
        }

        if (Set.class.isAssignableFrom(raw)) {
            // WARNING: DON'T REPLACE WITH LAMBDA OR IT NO WORK ):
            return getGeneric(typeArgs[0]).listOf()
                    .<Set>xmap(
                            list -> (Set<?>) new HashSet<>(list),
                            set -> List.copyOf(set)
                    );
        }

        if (Optional.class.isAssignableFrom(raw)) {
            return getGeneric(typeArgs[0]).optionalOf();
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
    public static <T> Endec<T> get(Class<T> clazz) {
        Endec<T> serializer = getOrNull(clazz);

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
    public static <T> Optional<Endec<T>> maybeGet(Class<T> clazz) {
        return Optional.ofNullable(getOrNull(clazz));
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable Endec<T> getOrNull(Class<T> clazz) {
        Endec<T> serializer = (Endec<T>) SERIALIZERS.get(clazz);

        if (serializer == null) {
            if (Record.class.isAssignableFrom(clazz)) {
                serializer = (Endec<T>) RecordEndec.create(conform(clazz, Record.class));
            } else if (clazz.isEnum()) {
                serializer = (Endec<T>) Endec.forEnum(conform(clazz, Enum.class));
            } else if (clazz.isArray()) {
                serializer = (Endec<T>) ReflectionEndecBuilder.createArrayEndec(clazz.getComponentType());
            } else if (clazz.isAnnotationPresent(SealedPolymorphic.class)) {
                serializer = (Endec<T>) ReflectionEndecBuilder.createSealedSerializer(clazz);
            } else {return null;}

            SERIALIZERS.put(clazz, serializer);
        }


        return serializer;
    }

    /**
     * Tries to create a serializer capable of
     * serializing arrays of the given element type
     *
     * @param elementClass The array element type
     * @return The created serializer
     */
    @SuppressWarnings("unchecked")
    public static Endec<?> createArrayEndec(Class<?> elementClass) {
        var elementSerializer = (Endec<Object>) get(elementClass);

        return elementSerializer.listOf().xmap(list -> {
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

    @SuppressWarnings("unchecked")
    private static Endec<?> createSealedSerializer(Class<?> commonClass) {
        if (!commonClass.isSealed()) {throw new IllegalStateException("@SealedPolymorphic class should be sealed!");}

        List<Class<?>> sortedPermittedSubclasses = Arrays.stream(commonClass.getPermittedSubclasses()).collect(Collectors.toList());

        for (int i = 0; i < sortedPermittedSubclasses.size(); i++) {
            Class<?> klass = sortedPermittedSubclasses.get(i);

            if (klass.isSealed()) {
                for (Class<?> subclass : klass.getPermittedSubclasses()) {
                    if (!sortedPermittedSubclasses.contains(subclass)) {sortedPermittedSubclasses.add(subclass);}
                }
            }
        }

        for (Class<?> klass : sortedPermittedSubclasses) {
            if (!klass.isSealed() && !Modifier.isFinal(klass.getModifiers())) {
                throw new IllegalStateException("Subclasses of a @SealedPolymorphic class must be sealed themselves!");
            }
        }

        sortedPermittedSubclasses.sort(Comparator.comparing(Class::getName));

        Int2ObjectMap<Endec<?>> serializerMap = new Int2ObjectOpenHashMap<>();
        Reference2IntMap<Class<?>> classesMap = new Reference2IntOpenHashMap<>();

        classesMap.defaultReturnValue(-1);

        for (int i = 0; i < sortedPermittedSubclasses.size(); i++) {
            Class<?> klass = sortedPermittedSubclasses.get(i);

            serializerMap.put(i, ReflectionEndecBuilder.get(klass));
            classesMap.put(klass, i);
        }

        return Endec.dispatched(serializerMap::get, v -> classesMap.getInt(v.getClass()), Endec.INT);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> conform(Class<?> clazz, Class<T> target) {
        return (Class<T>) clazz;
    }

    static {

        // ----------
        // Primitives
        // ----------

        register(Endec.BOOLEAN, Boolean.class, boolean.class);
        register(Endec.INT, Integer.class, int.class);
        register(Endec.LONG, Long.class, long.class);
        register(Endec.FLOAT, Float.class, float.class);
        register(Endec.DOUBLE, Double.class, double.class);

        register(Endec.BYTE, Byte.class, byte.class);
        register(Endec.SHORT, Short.class, short.class);
        register(Endec.SHORT.xmap(aShort -> (char) aShort.shortValue(), character -> (short) character.charValue()), Character.class, char.class);

        register(Void.class, Endec.VOID);

        // ----
        // Misc
        // ----

        register(String.class, Endec.STRING);
        register(UUID.class, BuiltInEndecs.UUID);
        register(Date.class, BuiltInEndecs.DATE);
        register(PacketByteBuf.class, BuiltInEndecs.PACKET_BYTE_BUF);

        // --------
        // MC Types
        // --------

        register(BlockPos.class, BuiltInEndecs.BLOCK_POS);
        register(ChunkPos.class, BuiltInEndecs.CHUNK_POS);
        register(ItemStack.class, BuiltInEndecs.ITEM_STACK);
        register(Identifier.class, BuiltInEndecs.IDENTIFIER);
        register(NbtCompound.class, NbtEndec.COMPOUND);
        register(
                BlockHitResult.class,
                new StructEndec<>() {
                    final Endec<Direction> DIRECTION = Endec.forEnum(Direction.class);

                    @Override
                    public void encodeStruct(Serializer.Struct struct, BlockHitResult hitResult) {
                        BlockPos blockPos = hitResult.getBlockPos();
                        struct.field("blockPos", BuiltInEndecs.BLOCK_POS, blockPos)
                                .field("side", DIRECTION, hitResult.getSide());

                        Vec3d vec3d = hitResult.getPos();
                        struct.field("x", Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("y", Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("z", Endec.FLOAT, (float) (vec3d.x - (double) blockPos.getX()))
                                .field("inside", Endec.BOOLEAN, hitResult.isInsideBlock());
                    }

                    @Override
                    public BlockHitResult decodeStruct(Deserializer.Struct struct) {
                        BlockPos blockPos = struct.field("blockPos", BuiltInEndecs.BLOCK_POS);
                        Direction direction = struct.field("side", DIRECTION);

                        float f = struct.field("x", Endec.FLOAT);
                        float g = struct.field("y", Endec.FLOAT);
                        float h = struct.field("z", Endec.FLOAT);

                        boolean bl = struct.field("inside", Endec.BOOLEAN);
                        return new BlockHitResult(
                                new Vec3d((double) blockPos.getX() + (double) f, (double) blockPos.getY() + (double) g, (double) blockPos.getZ() + (double) h), direction, blockPos, bl
                        );
                    }
                }
        );
        register(BitSet.class, BuiltInEndecs.BITSET);
        register(Text.class, BuiltInEndecs.TEXT);

        register(ParticleEffect.class,
                BuiltInEndecs.PACKET_BYTE_BUF.xmap(
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

        register(Vec3d.class, BuiltInEndecs.VEC3D);
        register(Vector3f.class, BuiltInEndecs.VECTOR3F);
        register(Vec3i.class, BuiltInEndecs.VEC3I);
    }
}
