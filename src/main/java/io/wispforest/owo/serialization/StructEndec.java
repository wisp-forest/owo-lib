package io.wispforest.owo.serialization;

import com.mojang.serialization.*;
import io.wispforest.owo.serialization.format.edm.*;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Marker and template interface for all endecs which serialize structs
 * <p>
 * Every such endec should extend this interface to profit from the implementation of {@link #mapCodec()}
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

    default MapCodec<T> mapCodec() {
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

                    return DataResult.success(StructEndec.this.decode(new LenientEdmDeserializer(EdmElement.wrapMap(map))));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }

            @Override
            public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
                try {
                    var element = StructEndec.this.encodeFully(EdmSerializer::new, input).<Map<String, EdmElement<?>>>cast();

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
