package io.wispforest.owo.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * A utility class for serializing data into {@link NbtCompound}
 * instances. {@link Type} instances are used for holding the
 * actual serializer functions while the key itself carries information
 * about what string key to use
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
     * Gets the stored data from the given NBT compound
     *
     * @param nbt The NBT to read from
     * @return The deserialized data
     */
    public T get(NbtCompound nbt) {
        return this.type.getter.apply(nbt, this.key);
    }

    /**
     * Gets the stored data from the given NBT compound
     *
     * @param nbt          The NBT to read from
     * @param defaultValue The default value to use if {@code nbt} does not
     *                     contain data corresponding to this key
     * @return The deserialized data, or {@code defaultValue} if no data was found
     */
    public T getOr(NbtCompound nbt, T defaultValue) {
        return nbt.contains(this.key, this.type.nbtEquivalent) ? this.get(nbt) : defaultValue;
    }

    /**
     * Stores the given data in the given NBT compound
     *
     * @param nbt   The NBT to write into
     * @param value The data to write
     */
    public void put(NbtCompound nbt, T value) {
        this.type.setter.accept(nbt, this.key, value);
    }

    /**
     * Removes data corresponding to this key's
     * {@code key} from the given NBT compound
     *
     * @param nbt The NBT to operate on
     */
    public void delete(NbtCompound nbt) {
        nbt.remove(this.key);
    }

    /**
     * @return {@code true} if the given NBT compound contains data of this key's type
     */
    public boolean isIn(NbtCompound nbt) {
        return nbt.contains(this.key, this.type.nbtEquivalent);
    }

    /**
     * Same as {@link #isIn(NbtCompound)} but also allows {@code nbt} to be null
     *
     * @param nbt The NBT to test against, or optionally {@code null}
     * @return {@code true} if the given NBT compound is not null
     * and contains data of this key's type
     */
    public boolean maybeIsIn(@Nullable NbtCompound nbt) {
        return nbt != null && nbt.contains(this.key, this.type.nbtEquivalent);
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
        public NbtList get(NbtCompound nbt) {
            return nbt.getList(this.key, this.elementType.nbtEquivalent);
        }

        @Override
        public NbtList getOr(NbtCompound nbt, NbtList defaultValue) {
            return nbt.contains(this.key, NbtElement.LIST_TYPE) ? this.get(nbt) : defaultValue;
        }

        @Override
        public void put(NbtCompound nbt, NbtList value) {
            nbt.put(this.key, value);
        }
    }

    /**
     * A container type holding serialization functions,
     * used for creating {@link NbtKey} instances
     *
     * @param <T> The type of object a given key can serialize
     */
    public static class Type<T> {
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

        private final byte nbtEquivalent;
        private final BiFunction<NbtCompound, String, T> getter;
        private final TriConsumer<NbtCompound, String, T> setter;

        private Type(byte nbtEquivalent, BiFunction<NbtCompound, String, T> getter, TriConsumer<NbtCompound, String, T> setter) {
            this.nbtEquivalent = nbtEquivalent;
            this.getter = getter;
            this.setter = setter;
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

        private static void writeItemStack(NbtCompound nbt, String key, ItemStack stack) {
            nbt.put(key, stack.writeNbt(new NbtCompound()));
        }

        private static ItemStack readItemStack(NbtCompound nbt, String key) {
            return nbt.contains(key, NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(nbt.getCompound(key)) : ItemStack.EMPTY;
        }
    }

}