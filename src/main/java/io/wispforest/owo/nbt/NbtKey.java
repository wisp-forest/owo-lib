package io.wispforest.owo.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A utility class for serializing data into {@link NbtCompound}
 * instances. {@link Type} instances are used for holding the
 * actual serializer functions while the key itself carries information
 * about what string key to use.
 * <p>
 * In order to conveniently use instances of this class, employ the methods
 * defined on {@link NbtCarrier} - this is interface-injected onto
 * {@link ItemStack} and {@link NbtCompound} by default
 *
 * @param <T> The type of data a given instance can serialize
 */
public class NbtKey<T> {

    protected final String key;
    protected final Type<T> type;

    /**
     * Creates a new key instance used for storing data of type
     * {@code T} into NBT compounds with the given string as key
     *
     * @param key  The string key to use as index into the NBT compound
     * @param type The type object that holds the serializer implementations
     */
    public NbtKey(String key, Type<T> type) {
        this.key = key;
        this.type = type;
    }

    /**
     * @deprecated Use {@link NbtCarrier#get(NbtKey)} instead
     */
    @Deprecated
    public T get(@NotNull NbtCompound nbt) {
        return this.type.getter.apply(nbt, this.key);
    }

    /**
     * @deprecated Use {@link NbtCarrier#put(NbtKey, T)} instead
     */
    public void put(@NotNull NbtCompound nbt, T value) {
        this.type.setter.accept(nbt, this.key, value);
    }

    /**
     * @deprecated Use {@link NbtCarrier#delete(NbtKey)} instead
     */
    public void delete(@NotNull NbtCompound nbt) {
        nbt.remove(this.key);
    }

    /**
     * @deprecated Use {@link NbtCarrier#has(NbtKey)} instead
     */
    public boolean isIn(@NotNull NbtCompound nbt) {
        return nbt.contains(this.key, this.type.nbtEquivalent);
    }

    /**
     * A {@link NbtKey} used for serializing a {@link NbtList} of
     * the given type
     *
     * @param <T> The type of elements in the list
     */
    public static final class ListKey<T> extends NbtKey<NbtList> {

        private final Type<T> elementType;

        public ListKey(String key, Type<T> elementType) {
            super(key, null);
            this.elementType = elementType;
        }

        @Override
        public NbtList get(@NotNull NbtCompound nbt) {
            return nbt.getList(this.key, this.elementType.nbtEquivalent);
        }

        @Override
        public void put(@NotNull NbtCompound nbt, NbtList value) {
            nbt.put(this.key, value);
        }

        @Override
        public boolean isIn(@NotNull NbtCompound nbt) {
            return nbt.contains(this.key, NbtElement.LIST_TYPE);
        }
    }

    /**
     * A container type holding serialization functions,
     * used for creating {@link NbtKey} instances
     */
    public static final class Type<T> {
        public static final Type<Byte> BYTE = new Type<>(NbtElement.BYTE_TYPE, NbtCompound::getByte, NbtCompound::putByte);
        public static final Type<Short> SHORT = new Type<>(NbtElement.SHORT_TYPE, NbtCompound::getShort, NbtCompound::putShort);
        public static final Type<Integer> INT = new Type<>(NbtElement.INT_TYPE, NbtCompound::getInt, NbtCompound::putInt);
        public static final Type<Long> LONG = new Type<>(NbtElement.LONG_TYPE, NbtCompound::getLong, NbtCompound::putLong);
        public static final Type<Float> FLOAT = new Type<>(NbtElement.FLOAT_TYPE, NbtCompound::getFloat, NbtCompound::putFloat);
        public static final Type<Double> DOUBLE = new Type<>(NbtElement.DOUBLE_TYPE, NbtCompound::getDouble, NbtCompound::putDouble);
        public static final Type<byte[]> BYTE_ARRAY = new Type<>(NbtElement.BYTE_ARRAY_TYPE, NbtCompound::getByteArray, NbtCompound::putByteArray);
        public static final Type<String> STRING = new Type<>(NbtElement.STRING_TYPE, NbtCompound::getString, NbtCompound::putString);
        public static final Type<NbtCompound> COMPOUND = new Type<>(NbtElement.COMPOUND_TYPE, NbtCompound::getCompound, NbtCompound::put);
        public static final Type<int[]> INT_ARRAY = new Type<>(NbtElement.INT_ARRAY_TYPE, NbtCompound::getIntArray, NbtCompound::putIntArray);
        public static final Type<long[]> LONG_ARRAY = new Type<>(NbtElement.LONG_ARRAY_TYPE, NbtCompound::getLongArray, NbtCompound::putLongArray);
        public static final Type<ItemStack> ITEM_STACK = new Type<>(NbtElement.COMPOUND_TYPE, Type::readItemStack, Type::writeItemStack);
        public static final Type<Identifier> IDENTIFIER = new Type<>(NbtElement.STRING_TYPE, Type::readIdentifier, Type::writeIdentifier);
        public static final Type<Boolean> BOOLEAN = new Type<>(NbtElement.BYTE_TYPE, NbtCompound::getBoolean, NbtCompound::putBoolean);

        private final byte nbtEquivalent;
        private final BiFunction<NbtCompound, String, T> getter;
        private final TriConsumer<NbtCompound, String, T> setter;

        private Type(byte nbtEquivalent, BiFunction<NbtCompound, String, T> getter, TriConsumer<NbtCompound, String, T> setter) {
            this.nbtEquivalent = nbtEquivalent;
            this.getter = getter;
            this.setter = setter;
        }

        /**
         * Creates a new type that applies the given functions on top of
         * this type. This allows easily composing types by abstracting away
         * the underlying NBT compound
         *
         * @param getter The getter function to convert from this type's value type to the new one
         * @param setter The setter function to convert from the new value type to this type's one
         * @param <R>    The value type of the created type
         * @return The new key
         */
        public <R> Type<R> then(Function<T, R> getter, Function<R, T> setter) {
            return new Type<>(this.nbtEquivalent,
                    (compound, s) -> getter.apply(this.getter.apply(compound, s)),
                    (compound, s, r) -> this.setter.accept(compound, s, setter.apply(r)));
        }

        /**
         * Creates a new {@link Type} that supports reading and writing data of type {@code T}
         * into {@link NbtCompound} instances. Use this if you want to store data that is
         * not supported by the default provided types
         *
         * @param nbtType The type of NBT element that is used to represent the data,
         *                see {@link NbtElement} for the relevant constants
         * @param getter  The function used for writing objects to an {@code NbtCompound}
         * @param setter  The function used for reading objects from an {@code NbtCompound}
         * @param <T>     The type of data the created key can serialize
         * @return The created Type instance
         */
        public static <T> Type<T> of(byte nbtType, BiFunction<NbtCompound, String, T> getter, TriConsumer<NbtCompound, String, T> setter) {
            return new Type<>(nbtType, getter, setter);
        }

        /**
         * Creates a new type that serializes registry entries of the given
         * registry using their ID in string form
         *
         * @param registry The registry of which to serialize entries
         * @param <T>      The type of registry entry to serialize
         * @return The created type
         */
        public static <T> Type<T> ofRegistry(Registry<T> registry) {
            return new Type<>(NbtElement.STRING_TYPE,
                    (compound, s) -> registry.get(new Identifier(compound.getString(s))),
                    (compound, s, t) -> compound.putString(s, registry.getId(t).toString()));
        }

        private static void writeItemStack(NbtCompound nbt, String key, ItemStack stack) {
            nbt.put(key, stack.writeNbt(new NbtCompound()));
        }

        private static ItemStack readItemStack(NbtCompound nbt, String key) {
            return nbt.contains(key, NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(nbt.getCompound(key)) : ItemStack.EMPTY;
        }

        private static void writeIdentifier(NbtCompound nbt, String key, Identifier identifier) {
            nbt.putString(key, identifier.toString());
        }

        private static Identifier readIdentifier(NbtCompound nbt, String key) {
            return nbt.contains(key, NbtElement.STRING_TYPE) ? new Identifier(nbt.getString(key)) : null;
        }
    }

}