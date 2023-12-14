package io.wispforest.owo.serialization.format.edm;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class EdmElement<T> {

    private final T value;
    private final Type type;

    private EdmElement(T value, Type type) {
        this.value = value;
        this.type = type;
    }

    public T value() {
        return this.value;
    }

    @SuppressWarnings("unchecked")
    public <V> V cast() {
        return (V) this.value;
    }

    public Type type() {
        return this.type;
    }

    public Object unwrap() {
        if (this.value instanceof List<?> list) {
            return list.stream().map(o -> ((EdmElement<?>) o).unwrap()).toList();
        } else if (this.value instanceof Map<?, ?> map) {
            return map.entrySet().stream().map(entry -> Map.entry(entry.getKey(), ((EdmElement<?>) entry.getValue()).unwrap())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else if (this.value instanceof Optional<?> optional) {
            return optional.map(o -> ((EdmElement<?>) o).unwrap());
        } else {
            return this.value;
        }
    }

    @Override
    public String toString() {
        return "E(" + this.type.name() + ", " + this.value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EdmElement<?> that = (EdmElement<?>) o;

        if (!this.value.equals(that.value)) return false;
        return this.type == that.type;
    }

    @Override
    public int hashCode() {
        int result = this.value.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
    }

    public static EdmElement<Byte> wrapByte(byte value) {
        return new EdmElement<>(value, Type.BYTE);
    }

    public static EdmElement<Short> wrapShort(short value) {
        return new EdmElement<>(value, Type.SHORT);
    }

    public static EdmElement<Integer> wrapInt(int value) {
        return new EdmElement<>(value, Type.INT);
    }

    public static EdmElement<Long> wrapLong(long value) {
        return new EdmElement<>(value, Type.LONG);
    }

    public static EdmElement<Float> wrapFloat(float value) {
        return new EdmElement<>(value, Type.FLOAT);
    }

    public static EdmElement<Double> wrapDouble(double value) {
        return new EdmElement<>(value, Type.DOUBLE);
    }

    public static EdmElement<Boolean> wrapBoolean(boolean value) {
        return new EdmElement<>(value, Type.BOOLEAN);
    }

    public static EdmElement<String> wrapString(String value) {
        return new EdmElement<>(value, Type.STRING);
    }

    public static EdmElement<byte[]> wrapBytes(byte[] value) {
        return new EdmElement<>(value, Type.BYTE);
    }

    public static EdmElement<Optional<EdmElement<?>>> wrapOptional(Optional<EdmElement<?>> value) {
        return new EdmElement<>(value, Type.OPTIONAL);
    }

    public static EdmElement<List<EdmElement<?>>> wrapSequence(List<EdmElement<?>> value) {
        return new EdmElement<>(value, Type.SEQUENCE);
    }

    public static EdmElement<Map<String, EdmElement<?>>> wrapMap(Map<String, EdmElement<?>> value) {
        return new EdmElement<>(value, Type.MAP);
    }

    public enum Type {
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,

        BOOLEAN,
        STRING,
        BYTES,
        OPTIONAL,

        SEQUENCE,
        MAP
    }
}
