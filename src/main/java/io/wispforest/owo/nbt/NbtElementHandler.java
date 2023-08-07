package io.wispforest.owo.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;

import java.util.function.Function;
import java.util.function.Supplier;

public class NbtElementHandler<T, E extends NbtElement> {
    public static final NbtElementHandler<Byte, NbtByte> BYTE = new NbtElementHandler<>(NbtElement.BYTE_TYPE, NbtByte::byteValue, NbtByte::of, () -> NbtByte.of((byte) 0));
    public static final NbtElementHandler<Short, NbtShort> SHORT = new NbtElementHandler<>(NbtElement.SHORT_TYPE, NbtShort::shortValue, NbtShort::of, () -> NbtShort.of((short) 0));
    public static final NbtElementHandler<Integer, NbtInt> INT = new NbtElementHandler<>(NbtElement.INT_TYPE, NbtInt::intValue, NbtInt::of, () -> NbtInt.of(0));
    public static final NbtElementHandler<Long, NbtLong> LONG = new NbtElementHandler<>(NbtElement.LONG_TYPE, NbtLong::longValue, NbtLong::of, () -> NbtLong.of(0L));
    public static final NbtElementHandler<Float, NbtFloat> FLOAT = new NbtElementHandler<>(NbtElement.FLOAT_TYPE, NbtFloat::floatValue, NbtFloat::of, () -> NbtFloat.of(0F));
    public static final NbtElementHandler<Double, NbtDouble> DOUBLE = new NbtElementHandler<>(NbtElement.DOUBLE_TYPE, NbtDouble::doubleValue, NbtDouble::of, () -> NbtDouble.of(0D));
    public static final NbtElementHandler<byte[], NbtByteArray> BYTE_ARRAY = new NbtElementHandler<>(NbtElement.BYTE_ARRAY_TYPE, NbtByteArray::getByteArray, NbtByteArray::new, () -> new NbtByteArray(new byte[0]));
    public static final NbtElementHandler<String, NbtString> STRING = new NbtElementHandler<>(NbtElement.STRING_TYPE, NbtString::asString, NbtString::of, () -> NbtString.of(""));
    public static final NbtElementHandler<NbtCompound, NbtCompound> COMPOUND = new NbtElementHandler<>(NbtElement.COMPOUND_TYPE, compound -> compound, compound -> compound, NbtCompound::new);
    public static final NbtElementHandler<int[], NbtIntArray> INT_ARRAY = new NbtElementHandler<>(NbtElement.INT_ARRAY_TYPE, NbtIntArray::getIntArray, NbtIntArray::new, () -> new NbtIntArray(new int[0]));
    public static final NbtElementHandler<long[], NbtLongArray> LONG_ARRAY = new NbtElementHandler<>(NbtElement.LONG_ARRAY_TYPE, NbtLongArray::getLongArray, NbtLongArray::new, () -> new NbtLongArray(new long[0]));
    public static final NbtElementHandler<ItemStack, NbtCompound> ITEM_STACK = new NbtElementHandler<>(NbtElement.COMPOUND_TYPE, NbtElementHandler::readItemStack, NbtElementHandler::writeItemStack, NbtCompound::new);
    public static final NbtElementHandler<Identifier, NbtString> IDENTIFIER = new NbtElementHandler<>(NbtElement.STRING_TYPE, NbtElementHandler::readIdentifier, NbtElementHandler::writeIdentifier, () -> NbtString.of("")); // Type::readIdentifier, Type::writeIdentifier
    public static final NbtElementHandler<Boolean, NbtByte> BOOLEAN = new NbtElementHandler<>(NbtElement.BYTE_TYPE, nbtByte -> nbtByte.byteValue() != 0, NbtByte::of, () -> NbtByte.of(false));

    protected final byte nbtEquivalent;

    protected final Function<T, E> toElement;
    protected final Function<E, T> fromElement;

    private final Supplier<E> defaultValue;

    private NbtElementHandler(byte nbtEquivalent, Function<E, T> fromElement, Function<T, E> toElement, Supplier<E> defaultValue) {
        this.nbtEquivalent = nbtEquivalent;

        this.toElement = toElement;
        this.fromElement = fromElement;

        this.defaultValue = defaultValue;
    }

    public <R> NbtElementHandler<R, E> then(Function<T, R> getter, Function<R, T> setter) {
        return new NbtElementHandler<>(this.nbtEquivalent,
                this.fromElement.andThen(getter),
                setter.andThen(this.toElement),
                defaultValue);
    }

    public static <T, E extends NbtElement> NbtElementHandler<T, E> of(byte nbtType, Function<E, T> fromElement, Function<T, E> toElement, Supplier<E> defaultValue) {
        return new NbtElementHandler<>(nbtType, fromElement, toElement, defaultValue);
    }

    public final <E extends NbtElement> E getElement(NbtCompound compound, String key) {
        return (E) (compound.contains(key, this.nbtEquivalent) ? compound.get(key) : this.defaultValue.get());
    }

    public final void setElement(NbtCompound compound, String key, NbtElement element) {
        compound.put(key, element);
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
