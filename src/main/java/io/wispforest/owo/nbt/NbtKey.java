package io.wispforest.owo.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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
    protected final Type<T, ?> type;

    /**
     * Creates a new key instance used for storing data of type
     * {@code T} into NBT compounds with the given string as key
     *
     * @param key  The string key to use as index into the NBT compound
     * @param type The type object that holds the serializer implementations
     */
    public NbtKey(String key, Type<T, ?> type) {
        this.key = key;
        this.type = type;
    }

    /**
     * @deprecated Use {@link NbtCarrier#get(NbtKey)} instead
     */
    @Deprecated
    public T get(@NotNull NbtCompound nbt) {
        return this.type.getter(nbt, this.key);
    }

    /**
     * @deprecated Use {@link NbtCarrier#put(NbtKey, T)} instead
     */
    @Deprecated
    public void put(@NotNull NbtCompound nbt, T value) {
        this.type.setter(nbt, this.key, value);
    }

    /**
     * @deprecated Use {@link NbtCarrier#delete(NbtKey)} instead
     */
    @Deprecated
    public void delete(@NotNull NbtCompound nbt) {
        nbt.remove(this.key);
    }

    /**
     * @deprecated Use {@link NbtCarrier#has(NbtKey)} instead
     */
    @Deprecated
    public boolean isIn(@NotNull NbtCompound nbt) {
        return nbt.contains(this.key, this.type.nbtEquivalent());
    }

    /**
     * A {@link NbtKey} used for serializing a {@link NbtList} of
     * the given type
     *
     * @param <T> The type of elements in the list
     */
    public static class ListKey<T> extends NbtKey<NbtList> {

        protected final Type<T, ?> elementType;

        public ListKey(String key, Type<T, ?> elementType) {
            super(key, null);
            this.elementType = elementType;
        }

        @Override
        public NbtList get(@NotNull NbtCompound nbt) {
            return nbt.getList(this.key, this.elementType.nbtEquivalent());
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
    public static final class Type<T, E extends NbtElement> {
        public static final Type<Byte, NbtByte> BYTE = new Type<>(NbtElement.BYTE_TYPE, NbtByte::byteValue, NbtByte::of, () -> NbtByte.of((byte) 0));
        public static final Type<Short, NbtShort> SHORT = new Type<>(NbtElement.SHORT_TYPE, NbtShort::shortValue, NbtShort::of, () -> NbtShort.of((short) 0));
        public static final Type<Integer, NbtInt> INT = new Type<>(NbtElement.INT_TYPE, NbtInt::intValue, NbtInt::of, () -> NbtInt.of(0));
        public static final Type<Long, NbtLong> LONG = new Type<>(NbtElement.LONG_TYPE, NbtLong::longValue, NbtLong::of, () -> NbtLong.of(0L));
        public static final Type<Float, NbtFloat> FLOAT = new Type<>(NbtElement.FLOAT_TYPE, NbtFloat::floatValue, NbtFloat::of, () -> NbtFloat.of(0F));
        public static final Type<Double, NbtDouble> DOUBLE = new Type<>(NbtElement.DOUBLE_TYPE, NbtDouble::doubleValue, NbtDouble::of, () -> NbtDouble.of(0D));
        public static final Type<byte[], NbtByteArray> BYTE_ARRAY = new Type<>(NbtElement.BYTE_ARRAY_TYPE, NbtByteArray::getByteArray, NbtByteArray::new, () -> new NbtByteArray(new byte[0]));
        public static final Type<String, NbtString> STRING = new Type<>(NbtElement.STRING_TYPE, NbtString::asString, NbtString::of, () -> NbtString.of(""));
        public static final Type<NbtCompound, NbtCompound> COMPOUND = new Type<>(NbtElement.COMPOUND_TYPE, compound -> compound, compound -> compound, NbtCompound::new);
        public static final Type<int[], NbtIntArray> INT_ARRAY = new Type<>(NbtElement.INT_ARRAY_TYPE, NbtIntArray::getIntArray, NbtIntArray::new, () -> new NbtIntArray(new int[0]));
        public static final Type<long[], NbtLongArray> LONG_ARRAY = new Type<>(NbtElement.LONG_ARRAY_TYPE, NbtLongArray::getLongArray, NbtLongArray::new, () -> new NbtLongArray(new long[0]));
        public static final Type<ItemStack, NbtCompound> ITEM_STACK = new Type<>(NbtElement.COMPOUND_TYPE, Type::readItemStack, Type::writeItemStack, NbtCompound::new);
        public static final Type<Identifier, NbtString> IDENTIFIER = new Type<>(NbtElement.STRING_TYPE, Type::readIdentifier, Type::writeIdentifier, () -> NbtString.of("")); // Type::readIdentifier, Type::writeIdentifier
        public static final Type<Boolean, NbtByte> BOOLEAN = new Type<>(NbtElement.BYTE_TYPE, nbtByte -> nbtByte.byteValue() != 0, NbtByte::of, () -> NbtByte.of(false));
        public static final Type<NbtList, NbtList> LIST = new Type<>(NbtElement.LIST_TYPE, nbtList -> nbtList, nbtList -> nbtList, NbtList::new);

        private final byte nbtEquivalent;

        private final Function<E, T> fromElement;
        private final Function<T, E> toElement;

        private final Supplier<E> defaultValue;

        private Type(byte nbtEquivalent, Function<E, T> fromElement, Function<T, E> toElement, Supplier<E> defaultValue) {
            this.nbtEquivalent = nbtEquivalent;

            this.fromElement = fromElement;
            this.toElement = toElement;

            this.defaultValue = defaultValue;
        }

        public T getter(NbtCompound compound, String key){
            return this.fromElement.apply((E) (compound.contains(key, this.nbtEquivalent) ? compound.get(key) : this.defaultValue.get()));
        }

        public void setter(NbtCompound compound, String key, T value){
            compound.put(key, this.toElement.apply(value));
        }

        private byte nbtEquivalent(){
            return this.nbtEquivalent;
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
        public <R> Type<R, E> then(Function<T, R> getter, Function<R, T> setter) {
            return new Type<>(this.nbtEquivalent, this.fromElement.andThen(getter), setter.andThen(this.toElement), this.defaultValue);
        }

        /**
         * Creates a new {@link Type} that supports reading and writing data of type {@code T}
         * into {@link NbtCompound} instances. Use this if you want to store data that is
         * not supported by the default provided types
         *
         * @param nbtType The type of NBT element that is used to represent the data,
         *                see {@link NbtElement} for the relevant constants
         * @param fromElement  The function used to convert from the type {@code T} to the required {@code NbtElement}
         * @param toElement    The function used to convert from the {@code NbtElement} to the specified type {@code T}
         * @param defaultValue The supplier to get a default instance if none are within {@link NbtCompound}
         * @param <T>     The type of data the created key can serialize
         * @return The created Type instance
         */
        public static <T, E extends NbtElement> Type<T, E> of(byte nbtType, Function<E, T> fromElement, Function<T, E> toElement, Supplier<E> defaultValue) {
            return new Type<>(nbtType, fromElement, toElement, defaultValue);
        }

        /**
         * Create a new type that serializes a Map of elements of the given Key {@link Type} and Value {@link Type}
         *
         * @param keyType    The {@link Type} used to serialize between {@link NbtCompound} and {@link Map.Entry} Key
         * @param valueType  The {@link Type} used to serialize between {@link NbtCompound} and {@link Map.Entry} Value
         * @param mapBuilder The builder used to create new instances of a Map
         * @param <K> The type of the key of the given map type
         * @param <V> The type of the value of the given map type
         * @param <M> The type of map the created Type can serialize
         * @return The map based Type instance
         */
        public static <K, V, M extends Map<K, V>> Type<M, ?> mapType(Type<K, ?> keyType, Type<V, ?> valueType, IntFunction<M> mapBuilder){
            Type<Set<Map.Entry<K,V>>, ?> setType = collectionType(
                    Type.COMPOUND.then(
                            compound -> Map.entry(keyType.getter(compound, "key"), valueType.getter(compound, "value")),
                            (Map.Entry<K, V> entry) -> {
                                NbtCompound compound = new NbtCompound();

                                keyType.setter(compound, "key", entry.getKey());
                                valueType.setter(compound, "value", entry.getValue());

                                return compound;
                            }),
                    HashSet::new);

            return setType.then(entries -> {
                M returnMap = mapBuilder.apply(entries.size());

                for (Map.Entry<K, V> entry : entries) returnMap.put(entry.getKey(), entry.getValue());

                return returnMap;
            }, Map::entrySet);
        }

        /**
         * Creates a new type that serializes a List of elements of the given {@link Type}
         *
         * @param elementType The {@link Type} base used to serialize between {@link NbtList} elements
         * @param <T>         The type of data the passed key can serialize
         * @return The List based Type instance
         */
        public static <T> Type<List<T>, ?> listType(Type<T, ?> elementType){
            return collectionType(elementType, ArrayList::new);
        }

        /**
         *
         * @param elementType       The {@link Type} base key used to serialize between {@link NbtList}
         * @param collectionBuilder The builder used to create new instances of a collection
         * @param <T>               The type of data the passed key can serialize
         * @param <C>               The type of collection the created Type can serialize
         * @return The Collection based type instance
         */
        public static <T, C extends Collection<T>, E extends NbtElement> Type<C, ?> collectionType(Type<T, ?> elementType, IntFunction<C> collectionBuilder){
            return Type.LIST.then(
                    nbtList -> {
                        C collection = collectionBuilder.apply(nbtList.size());

                        for (NbtElement element : nbtList) collection.add(((Function<E, T>) elementType.fromElement).apply((E) element));

                        return collection;
                    },
                    values -> {
                        NbtList nbtList = new NbtList();

                        for(T value : values) nbtList.add(elementType.toElement.apply(value));

                        return nbtList;
                    }
            );
        }

        /**
         * Creates a new type that serializes registry entries of the given
         * registry using their ID in string form
         *
         * @param registry The registry of which to serialize entries
         * @param <T>      The type of registry entry to serialize
         * @return The created type
         */
        public static <T> Type<T, NbtString> ofRegistry(Registry<T> registry) {
            return Type.IDENTIFIER.then(registry::get, registry::getId);
        }

        private static NbtCompound writeItemStack(ItemStack stack) {
            return stack.writeNbt(new NbtCompound());
        }

        private static ItemStack readItemStack(NbtCompound nbt) {
            return nbt.isEmpty() ? ItemStack.fromNbt(nbt) : ItemStack.EMPTY;
        }

        private static NbtString writeIdentifier(Identifier identifier) {
            return NbtString.of(identifier.toString());
        }

        private static Identifier readIdentifier(NbtString nbtString) {
            return new Identifier(nbtString.asString());
        }
    }
}