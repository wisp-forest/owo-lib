package io.wispforest.owo.serialization.format.edm;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.format.edm.EdmElement;
import io.wispforest.owo.serialization.format.ContextHolder;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EdmOps implements DynamicOps<EdmElement<?>>, ContextHolder {

    private static final EdmOps NO_CONTEXT = new EdmOps(SerializationContext.empty());

    private final SerializationContext capturedContext;
    private EdmOps(SerializationContext capturedContext) {
        this.capturedContext = capturedContext;
    }

    public static EdmOps withContext(SerializationContext context) {
        return new EdmOps(context);
    }

    public static EdmOps withoutContext() {
        return NO_CONTEXT;
    }

    @Override
    public SerializationContext capturedContext() {
        return this.capturedContext;
    }

    // --- Serialization ---

    @Override
    public EdmElement<?> empty() {
        return EdmElement.EMPTY;
    }

    public EdmElement<?> createNumeric(Number number) {
        return EdmElement.wrapDouble(number.doubleValue());
    }

    public EdmElement<?> createByte(byte b) {
        return EdmElement.wrapByte(b);
    }

    public EdmElement<?> createShort(short s) {
        return EdmElement.wrapShort(s);
    }

    public EdmElement<?> createInt(int i) {
        return EdmElement.wrapInt(i);
    }

    public EdmElement<?> createLong(long l) {
        return EdmElement.wrapLong(l);
    }

    public EdmElement<?> createFloat(float f) {
        return EdmElement.wrapFloat(f);
    }

    public EdmElement<?> createDouble(double d) {
        return EdmElement.wrapDouble(d);
    }

    // ---

    public EdmElement<?> createBoolean(boolean bl) {
        return EdmElement.wrapBoolean(bl);
    }

    @Override
    public EdmElement<?> createString(String value) {
        return EdmElement.wrapString(value);
    }

    @Override
    public EdmElement<?> createByteList(ByteBuffer input) {
        return EdmElement.wrapBytes(DataFixUtils.toArray(input));
    }

    // ---

    @Override
    public EdmElement<?> createList(Stream<EdmElement<?>> input) {
        return EdmElement.wrapSequence(input.toList());
    }

    @Override
    public DataResult<EdmElement<?>> mergeToList(EdmElement<?> list, EdmElement<?> value) {
        if (list == empty()) {
            return DataResult.success(EdmElement.wrapSequence(List.of(value)));
        } else if (list != null && list.value() instanceof List<?> properList) {
            var newList = new ArrayList<EdmElement<?>>((Collection<? extends EdmElement<?>>) properList);
            newList.add(value);

            return DataResult.success(EdmElement.wrapSequence(newList));
        } else {
            return DataResult.error(() -> "mergeToList called with not a list: " + list);
        }
    }

    @Override
    public EdmElement<?> createMap(Stream<Pair<EdmElement<?>, EdmElement<?>>> map) {
        return EdmElement.wrapMap(map.collect(Collectors.toMap(pair -> pair.getFirst().cast(), Pair::getSecond)));
    }

    @Override
    public DataResult<EdmElement<?>> mergeToMap(EdmElement<?> map, EdmElement<?> key, EdmElement<?> value) {
        if (key == null || !(key.value() instanceof String)) {
            return DataResult.error(() -> "Key is not a string: " + key);
        }

        if (map == empty()) {
            return DataResult.success(EdmElement.wrapMap(Map.of(key.cast(), value)));
        } else if (map.value() instanceof Map<?, ?> properMap) {
            var newMap = new HashMap<String, EdmElement<?>>((Map<String, ? extends EdmElement<?>>) properMap);
            newMap.put(key.cast(), value);

            return DataResult.success(EdmElement.wrapMap(newMap));
        } else {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map);
        }
    }

    // --- Deserialization ---

    private static <T> DataResult<T> unpack(EdmElement<?> input,
                                            Function<Object, @Nullable DataResult<T>> checkFunc,
                                            Function<@Nullable EdmElement<?>, String> func) {
        return unpack(input, checkFunc, null, func);
    }

    private static <T> DataResult<T> unpack(EdmElement<?> input,
                                            Function<Object, @Nullable DataResult<T>> checkFunc1,
                                            Function<Object, @Nullable DataResult<T>> checkFunc2,
                                            Function<@Nullable EdmElement<?>, String> func) {
        if (input != null) {
            var value = input.value();
            var result = checkFunc1.apply(value);
            if (result == null && checkFunc2 != null) result = checkFunc2.apply(value);
            if (result != null) return result;
        }
        return DataResult.error(() -> func.apply(input));
    }

    @Override
    public DataResult<Number> getNumberValue(EdmElement<?> input) {
        return unpack(input, o -> o instanceof Number number ? DataResult.success(number) : null, element -> "Not a number: " + element);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(EdmElement<?> input) {
        return unpack(input,
            o -> o instanceof Boolean bl ? DataResult.success(bl) : null,
            o -> o instanceof Byte b ? DataResult.success(b == 1) : null,
            (element) -> "Not a boolean: " + element
        );
    }

    @Override
    public DataResult<String> getStringValue(EdmElement<?> input) {
        return unpack(input, o -> o instanceof String string ? DataResult.success(string) : null, element -> "Not a string: " + element);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(EdmElement<?> input) {
        return unpack(input, o -> o instanceof byte[] bytes ? DataResult.success(ByteBuffer.wrap(bytes)) : null, element -> "Not bytes: " + element);
    }

    // ---

    @Override
    public DataResult<Stream<EdmElement<?>>> getStream(EdmElement<?> input) {
        return unpack(input,
            o -> input == this.empty() ? DataResult.success(Stream.of()) : null,
            o -> o instanceof List<?> list ? DataResult.success(list.stream().map(v -> (EdmElement<?>) v)) : null,
            (element) -> "Not a sequence: " + element
        );
    }

    @Override
    public DataResult<Stream<Pair<EdmElement<?>, EdmElement<?>>>> getMapValues(EdmElement<?> input) {
        return unpack(input,
            o -> input == this.empty() ? DataResult.success(Stream.of()) : null,
            o -> o instanceof Map<?, ?> map ? DataResult.success(map.entrySet().stream().map(entry -> new Pair(EdmElement.wrapString((String) entry.getKey()), entry.getValue()))) : null,
            (element) -> "Not a map:: " + element
        );
    }

    // ---

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, EdmElement<?> input) {
        if (input == null) throw new NullPointerException("EdmOps.convertTo was passed a null value to convert!");
        if (input == this.empty()) return outOps.empty();
        return switch (input.type()) {
            case BYTE -> outOps.createByte(input.cast());
            case SHORT -> outOps.createShort(input.cast());
            case INT -> outOps.createInt(input.cast());
            case LONG -> outOps.createLong(input.cast());
            case FLOAT -> outOps.createFloat(input.cast());
            case DOUBLE -> outOps.createDouble(input.cast());
            case BOOLEAN -> outOps.createBoolean(input.cast());
            case STRING -> outOps.createString(input.cast());
            case BYTES -> outOps.createByteList(ByteBuffer.wrap(input.cast()));
            case OPTIONAL -> input.<Optional<EdmElement<?>>>cast().map(element -> this.convertTo(outOps, element)).orElse(outOps.empty());
            case SEQUENCE -> outOps.createList(input.<List<EdmElement<?>>>cast().stream().map(element -> this.convertTo(outOps, element)));
            case MAP ->
                    outOps.createMap(input.<Map<String, EdmElement<?>>>cast().entrySet().stream().map(entry -> new Pair<>(outOps.createString(entry.getKey()), this.convertTo(outOps, entry.getValue()))));
        };
    }

    @Override
    public EdmElement<?> remove(EdmElement<?> input, String key) {
        if (input != null && input.value() instanceof Map<?, ?> map) {
            var newMap = new HashMap<String, EdmElement<?>>((Map<? extends String, ? extends EdmElement<?>>) map);
            newMap.remove(key);

            return EdmElement.wrapMap(newMap);
        } else {
            return input;
        }
    }
}
