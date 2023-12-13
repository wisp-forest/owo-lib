package io.wispforest.owo.serialization;

import com.mojang.serialization.*;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import io.wispforest.owo.serialization.format.edm.EdmDeserializer;
import io.wispforest.owo.serialization.format.edm.EdmElement;
import io.wispforest.owo.serialization.format.edm.EdmOps;
import io.wispforest.owo.serialization.format.edm.EdmSerializer;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Helper Interface for Structs that do not conform to the {@link StructEndecBuilder} format
 */
public interface StructEndec<T> extends Endec<T> {

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

    void encodeStruct(Serializer.Struct struct, T value);

    T decodeStruct(Deserializer.Struct struct);

    default MapCodec<T> mapCodec(){
        return new MapCodec<>() {
            @Override
            public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
                return Stream.of();
            }

            @Override
            public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
                try {
                    Map<String, EdmElement<?>> map = new HashMap<>();

                    input.entries().forEach(pair -> {
                        var key = Util.getResult(
                                ops.getStringValue(pair.getFirst()),
                                s -> new IllegalStateException("Unable to parse given key value: " + s));

                        map.put(key, ops.convertTo(EdmOps.INSTANCE, pair.getSecond()));
                    });

                    return DataResult.success(StructEndec.this.decode(new EdmDeserializer(EdmElement.wrapMap(map))));
                } catch (Exception e){
                    return DataResult.error(e::getMessage);
                }
            }

            @Override
            public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
                try {
                    Map<String, EdmElement<?>> element = StructEndec.this.encodeFully(EdmSerializer::new, input).cast();

                    for (var entry : element.entrySet()) {
                        prefix = prefix.add(entry.getKey(), EdmOps.INSTANCE.convertTo(ops, entry.getValue()));
                    }

                    return prefix;
                } catch (Exception e){
                    return prefix.withErrorsFrom(DataResult.error(e::getMessage, input));
                }
            }
        };
    }
}
