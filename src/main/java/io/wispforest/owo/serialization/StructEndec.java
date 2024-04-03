package io.wispforest.owo.serialization;

import com.mojang.serialization.*;
import io.wispforest.owo.serialization.endec.StructField;
import io.wispforest.owo.serialization.format.edm.*;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Marker and template interface for all endecs which serialize structs
 * <p>
 * Every such endec should extend this interface to profit from the implementation of {@link #mapCodec(SerializationAttribute...)}
 * and composability which allows {@link Endec#dispatchedStruct(Function, Function, Endec, String)} to work
 */
public interface StructEndec<T> extends Endec<T> {

    void encodeStruct(Serializer.Struct struct, T value);

    T decodeStruct(Deserializer.Struct struct);

    @Override
    default void encode(Serializer<?> serializer, T value) {
        try (var struct = serializer.struct()) {
            this.encodeStruct(struct, value);
        }
    }

    @Override
    default T decode(Deserializer<?> deserializer) {
        return this.decodeStruct(deserializer.struct());
    }

    default <S> StructField<S, T> flatFieldOf(Function<S, T> getter) {
        return new StructField.Flat<>(this, getter);
    }

    @Override
    default <R> StructEndec<R> xmap(Function<T, R> to, Function<R, T> from) {
        return new StructEndec<>() {
            @Override
            public void encodeStruct(Serializer.Struct struct, R value) {
                StructEndec.this.encodeStruct(struct, from.apply(value));
            }
            @Override
            public R decodeStruct(Deserializer.Struct struct) {
                return to.apply(StructEndec.this.decodeStruct(struct));
            }
        };
    }

    default MapCodec<T> mapCodec(SerializationAttribute... assumedAttributes) {
        return new MapCodec<>() {
            @Override
            public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
                throw new UnsupportedOperationException("MapCodec generated from StructEndec cannot report keys");
            }

            @Override
            public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
                try {
                    var map = new HashMap<String, EdmElement<?>>();
                    input.entries().forEach(pair -> {
                        map.put(
                                Util.getResult(
                                        ops.getStringValue(pair.getFirst()),
                                        s -> new IllegalStateException("Unable to parse key: " + s)
                                ),
                                ops.convertTo(EdmOps.INSTANCE, pair.getSecond())
                        );
                    });

                    return DataResult.success(StructEndec.this.decode(LenientEdmDeserializer.of(EdmElement.wrapMap(map)).withAttributes(assumedAttributes)));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }

            @Override
            public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
                try {
                    var element = StructEndec.this.encodeFully(() -> EdmSerializer.of().withAttributes(assumedAttributes), input).<Map<String, EdmElement<?>>>cast();

                    var result = prefix;
                    for (var entry : element.entrySet()) {
                        result = result.add(entry.getKey(), EdmOps.INSTANCE.convertTo(ops, entry.getValue()));
                    }

                    return result;
                } catch (Exception e) {
                    return prefix.withErrorsFrom(DataResult.error(e::getMessage, input));
                }
            }
        };
    }
}
