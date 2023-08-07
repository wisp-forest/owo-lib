package io.wispforest.owo.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
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
    @Deprecated
    public void put(@NotNull NbtCompound nbt, T value) {
        this.type.setter.accept(nbt, this.key, value);
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
        return nbt.contains(this.key, this.type.nbtEquivalent.get());
    }

    /**
     * A {@link NbtKey} used for serializing a {@link NbtList} of
     * the given type
     *
     * @param <T> The type of elements in the list
     */
    @Deprecated
    public static final class ListKey<T> extends NbtKey<NbtList> {

        private final Type<T> elementType;

        public ListKey(String key, Type<T> elementType) {
            super(key, null);
            this.elementType = elementType;
        }

        @Override
        public NbtList get(@NotNull NbtCompound nbt) {
            return nbt.getList(this.key, this.elementType.nbtEquivalent.get());
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
        public static final Type<Byte> BYTE = new Type<>(NbtElementHandler.BYTE);
        public static final Type<Short> SHORT = new Type<>(NbtElementHandler.SHORT);
        public static final Type<Integer> INT = new Type<>(NbtElementHandler.INT);
        public static final Type<Long> LONG = new Type<>(NbtElementHandler.LONG);
        public static final Type<Float> FLOAT = new Type<>(NbtElementHandler.FLOAT);
        public static final Type<Double> DOUBLE = new Type<>(NbtElementHandler.DOUBLE);
        public static final Type<byte[]> BYTE_ARRAY = new Type<>(NbtElementHandler.BYTE_ARRAY);
        public static final Type<String> STRING = new Type<>(NbtElementHandler.STRING);
        public static final Type<NbtCompound> COMPOUND = new Type<>(NbtElementHandler.COMPOUND);
        public static final Type<int[]> INT_ARRAY = new Type<>(NbtElementHandler.INT_ARRAY);
        public static final Type<long[]> LONG_ARRAY = new Type<>(NbtElementHandler.LONG_ARRAY);
        public static final Type<ItemStack> ITEM_STACK = new Type<>(NbtElementHandler.ITEM_STACK);
        public static final Type<Identifier> IDENTIFIER = new Type<>(NbtElementHandler.IDENTIFIER);
        public static final Type<Boolean> BOOLEAN = new Type<>(NbtElementHandler.BOOLEAN);

        @Nullable
        private NbtElementHandler<T, ? extends NbtElement> handler = null;

        private final Supplier<Byte> nbtEquivalent;
        private final BiFunction<NbtCompound, String, T> getter;
        private final TriConsumer<NbtCompound, String, T> setter;

        private Type(@NotNull NbtElementHandler<T, ? extends NbtElement> handler) {
            this(() -> handler.nbtEquivalent, (compound, s) -> handler.fromElement.apply(handler.getElement(compound, s)), (compound, s, t) -> handler.setElement(compound, s, handler.toElement.apply(t)));

            this.handler = handler;
        }

        @Deprecated
        private Type(Supplier<Byte> nbtEquivalent, BiFunction<NbtCompound, String, T> getter, TriConsumer<NbtCompound, String, T> setter) {
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
            if(handler == null){
                return new Type<>(this.nbtEquivalent,
                        (compound, s) -> getter.apply(this.getter.apply(compound, s)),
                        (compound, s, r) -> this.setter.accept(compound, s, setter.apply(r)));
            }

            return new Type<>(handler.then(getter, setter));
        }

        /**
         * @deprecated Use {@link Type#of(byte, Function, Function, Supplier)} instead
         */
        @Deprecated
        public static <T> Type<T> of(byte nbtType, BiFunction<NbtCompound, String, T> getter, TriConsumer<NbtCompound, String, T> setter) {
            return new Type<>(() -> nbtType, getter, setter);
        }

        /**
         * Creates a new {@link Type} that supports reading and writing data of type {@code T}
         * into {@link NbtCompound} instances using {@link NbtElementHandler}. Use this if you want to store data that is
         * not supported by the default provided types
         *
         * @param nbtType The type of NBT element that is used to represent the data,
         *                see {@link NbtElement} for the relevant constants
         * @param fromElement  The function used to convert from the type {@code T} to the required {@code NbtElement}
         * @param toElement    The function used to convert from the {@code NbtElement} to the specified type {@code T}
         * @param <T>     The type of data the created key can serialize
         * @return The created Type instance
         */
        public static <T, E extends NbtElement> Type<T> of(byte nbtType, Function<E, T> fromElement, Function<T, E> toElement, Supplier<E> defaultValue) {
            return new Type<>(NbtElementHandler.of(nbtType, fromElement, toElement, defaultValue));
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
            return new Type<>(NbtElementHandler.IDENTIFIER.then(registry::get, registry::getId));
        }
    }

    public static final class CollectionKey<T, C extends Collection<T>> extends NbtKey<NbtList> {
        private final Type<T> elementType;

        private final IntFunction<C> collectionBuilder;

        public CollectionKey(String key, Type<T> elementType, IntFunction<C> collectionBuilder) {
            super(key, null);

            if(elementType.handler == null){
                throw new IllegalArgumentException("A CollectionKey was attempted to be constructed with a NbtKey.Type that was not created with a NbtElementHandler which is required!");
            }

            this.elementType = elementType;
            this.collectionBuilder = collectionBuilder;
        }

        public <E extends NbtElement> C getCollection(@NotNull NbtCompound nbt) {
            NbtList nbtList = get(nbt);

            C collection = collectionBuilder.apply(nbtList.size());

            for (NbtElement element : nbtList) collection.add(((Function<E, T>) elementType.handler.fromElement).apply((E) element));

            return collection;
        }

        public void putCollection(@NotNull NbtCompound nbt, C values) {
            NbtList nbtList = new NbtList();

            for(T value : values) nbtList.add(elementType.handler.toElement.apply(value));

            put(nbt, nbtList);
        }

        public Iterator<T> iterator(NbtCompound nbt){
            return new NbtListIterator<>(get(nbt), elementType.handler.fromElement);
        }

        @Override
        public NbtList get(@NotNull NbtCompound nbt) {
            return nbt.getList(this.key, this.elementType.nbtEquivalent.get());
        }

        @Override
        public void put(@NotNull NbtCompound nbt, NbtList value) {
            nbt.put(this.key, value);
        }

        @Override
        public boolean isIn(@NotNull NbtCompound nbt) {
            return nbt.contains(this.key, NbtElement.LIST_TYPE);
        }

        public static class NbtListIterator<T, E extends NbtElement> implements Iterator<T> {
            private final Iterator<NbtElement> listIterator;
            private final Function<E, T> getter;

            public NbtListIterator(List<NbtElement> listIterator, Function<E, T> getter){
                this.listIterator = listIterator.iterator();

                this.getter = getter;
            }

            @Override
            public boolean hasNext() {
                return listIterator.hasNext();
            }

            @Override
            public T next() {
                return this.getter.apply((E) listIterator.next());
            }
        }
    }

}