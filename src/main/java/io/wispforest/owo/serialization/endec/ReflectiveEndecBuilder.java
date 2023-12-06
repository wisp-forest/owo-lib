package io.wispforest.owo.serialization.endec;

import io.wispforest.owo.network.serialization.SealedPolymorphic;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.StructEndec;
import io.wispforest.owo.serialization.format.nbt.NbtEndec;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import java.util.stream.Collectors;

public class ReflectiveEndecBuilder {

    private static final Map<Class<?>, Endec<?>> CLASS_TO_ENDEC = new HashMap<>();

    /**
     * Register {@code endec} to be used for (de)serializing instances of {@code clazz}
     */
    public static <T> void register(Endec<T> endec, Class<T> clazz) {
        if (CLASS_TO_ENDEC.containsKey(clazz)) {
            throw new IllegalStateException("Class '" + clazz.getName() + "' already has an associated endec");
        }

        CLASS_TO_ENDEC.put(clazz, endec);
    }

    /**
     * Invoke {@link #register(Endec, Class)} once for each class of {@code classes}
     */
    @SafeVarargs
    private static <T> void register(Endec<T> endec, Class<T>... classes) {
        for (var clazz : classes) register(endec, clazz);
    }

    /**
     * Get (or potentially create) the endec associated with {@code type}. In addition
     * to {@link #get(Class)}, this method uses type parameter information to automatically
     * create endecs for maps, lists, sets and optionals.
     * <p>
     * If {@code type} is none of the above, it is simply forwarded to {@link #get(Class)}
     */
    @SuppressWarnings("unchecked")
    public static Endec<?> get(Type type) {
        if (type instanceof Class<?> clazz) return get(clazz);

        var parameterized = (ParameterizedType) type;
        var raw = (Class<?>) parameterized.getRawType();
        var typeArgs = parameterized.getActualTypeArguments();

        if (raw == Map.class) {
            return typeArgs[0] == String.class
                    ? get(typeArgs[1]).mapOf()
                    : Endec.map(get(typeArgs[0]), get(typeArgs[1]));
        }

        if (raw == List.class) {
            return get(typeArgs[0]).listOf();
        }

        if (raw == Set.class) {
            //noinspection rawtypes,Convert2MethodRef
            return get(typeArgs[0]).listOf().<Set>xmap(
                    list -> (Set<?>) new HashSet<>(list),
                    set -> List.copyOf(set)
            );
        }

        if (raw == Optional.class) {
            return get(typeArgs[0]).optionalOf();
        }

        return get(raw);
    }

    /**
     * Get (or potentially create) the endec associated with {@code clazz},
     * throwing if no such endec is registered and cannot automatically be created
     * <p>
     * Classes for which endecs can be generated are: records, enums, arrays and sealed
     * classes annotated with {@link SealedPolymorphic}
     */
    public static <T> Endec<T> get(Class<T> clazz) {
        var endec = getOrNull(clazz);
        if (endec == null) {
            throw new IllegalStateException("No endec available for class '" + clazz.getName() + "'");
        }

        return endec;
    }

    /**
     * Non-throwing equivalent of {@link #get(Class)}
     */
    public static <T> Optional<Endec<T>> maybeGet(Class<T> clazz) {
        return Optional.ofNullable(getOrNull(clazz));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> @Nullable Endec<T> getOrNull(Class<T> clazz) {
        Endec<T> serializer = (Endec<T>) CLASS_TO_ENDEC.get(clazz);

        if (serializer == null) {
            if (Record.class.isAssignableFrom(clazz)) {
                serializer = (Endec<T>) RecordEndec.create((Class<? extends Record>) clazz);
            } else if (clazz.isEnum()) {
                serializer = (Endec<T>) Endec.forEnum((Class<? extends Enum>) clazz);
            } else if (clazz.isArray()) {
                serializer = (Endec<T>) ReflectiveEndecBuilder.createArrayEndec(clazz.getComponentType());
            } else if (clazz.isAnnotationPresent(SealedPolymorphic.class)) {
                serializer = (Endec<T>) ReflectiveEndecBuilder.createSealedSerializer(clazz);
            } else {
                return null;
            }

            CLASS_TO_ENDEC.put(clazz, serializer);
        }


        return serializer;
    }

    @SuppressWarnings("unchecked")
    private static Endec<?> createArrayEndec(Class<?> elementClass) {
        var elementEndec = (Endec<Object>) get(elementClass);

        return elementEndec.listOf().xmap(list -> {
            int length = list.size();
            var array = Array.newInstance(elementClass, length);
            for (int i = 0; i < length; i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }, t -> {
            int length = Array.getLength(t);
            var list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(Array.get(t, i));
            }
            return list;
        });
    }

    private static Endec<?> createSealedSerializer(Class<?> commonClass) {
        if (!commonClass.isSealed()) {
            throw new IllegalStateException("@SealedPolymorphic class must be sealed");
        }

        var permittedSubclasses = Arrays.stream(commonClass.getPermittedSubclasses()).collect(Collectors.toList());

        for (int i = 0; i < permittedSubclasses.size(); i++) {
            var clazz = permittedSubclasses.get(i);

            if (clazz.isSealed()) {
                for (var subclass : clazz.getPermittedSubclasses()) {
                    if (!permittedSubclasses.contains(subclass)) permittedSubclasses.add(subclass);
                }
            }
        }

        for (var clazz : permittedSubclasses) {
            if (!clazz.isSealed() && !Modifier.isFinal(clazz.getModifiers())) {
                throw new IllegalStateException("Subclasses of a @SealedPolymorphic class must themselves be sealed");
            }
        }

        permittedSubclasses.sort(Comparator.comparing(Class::getName));

        var serializerMap = new Int2ObjectOpenHashMap<Endec<?>>();
        var classesMap = new Reference2IntOpenHashMap<Class<?>>();

        classesMap.defaultReturnValue(-1);

        for (int i = 0; i < permittedSubclasses.size(); i++) {
            Class<?> klass = permittedSubclasses.get(i);

            serializerMap.put(i, ReflectiveEndecBuilder.get(klass));
            classesMap.put(klass, i);
        }

        return Endec.dispatched(integer -> serializerMap.get(integer.intValue()), instance -> classesMap.getInt(instance.getClass()), Endec.INT);
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

        register(Endec.VOID, Void.class);

        // ----
        // Misc
        // ----

        register(Endec.STRING, String.class);
        register(BuiltInEndecs.UUID, UUID.class);
        register(BuiltInEndecs.DATE, Date.class);
        register(BuiltInEndecs.PACKET_BYTE_BUF, PacketByteBuf.class);

        // --------
        // MC Types
        // --------

        register(BuiltInEndecs.BLOCK_POS, BlockPos.class);
        register(BuiltInEndecs.CHUNK_POS, ChunkPos.class);
        register(BuiltInEndecs.ITEM_STACK, ItemStack.class);
        register(BuiltInEndecs.IDENTIFIER, Identifier.class);
        register(NbtEndec.COMPOUND, NbtCompound.class);
        register(
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
                },
                BlockHitResult.class
        );
        register(BuiltInEndecs.BITSET, BitSet.class);
        register(BuiltInEndecs.TEXT, Text.class);

        register(BuiltInEndecs.PACKET_BYTE_BUF.xmap(
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
        ), ParticleEffect.class);

        register(BuiltInEndecs.VEC3D, Vec3d.class);
        register(BuiltInEndecs.VECTOR3F, Vector3f.class);
        register(BuiltInEndecs.VEC3I, Vec3i.class);
    }
}
