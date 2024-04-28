package io.wispforest.owo.serialization;

import com.mojang.serialization.*;
import io.wispforest.owo.mixin.ForwardingDynamicOpsAccessor;
import io.wispforest.owo.mixin.RegistryOpsAccessor;
import io.wispforest.owo.serialization.endec.StructField;
import io.wispforest.owo.serialization.format.edm.*;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.dynamic.ForwardingDynamicOps;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Marker and template interface for all endecs which serialize structs
 * <p>
 * Every such endec should extend this interface to profit from the implementation of {@link #mapCodec(SerializationContext)}
 * and composability which allows {@link Endec#dispatchedStruct(Function, Function, Endec, String)} to work
 */
public interface StructEndec<T> extends Endec<T> {

    void encodeStruct(SerializationContext ctx, Serializer.Struct struct, T value);

    T decodeStruct(SerializationContext ctx, Deserializer.Struct struct);

    @Override
    default void encode(SerializationContext ctx, Serializer<?> serializer, T value) {
        try (var struct = serializer.struct()) {
            this.encodeStruct(ctx, struct, value);
        }
    }

    @Override
    default T decode(SerializationContext ctx, Deserializer<?> deserializer) {
        return this.decodeStruct(ctx, deserializer.struct());
    }

    default <S> StructField<S, T> flatFieldOf(Function<S, T> getter) {
        return new StructField.Flat<>(this, getter);
    }

    @Override
    default <R> StructEndec<R> xmap(Function<T, R> to, Function<R, T> from) {
        return new StructEndec<>() {
            @Override
            public void encodeStruct(SerializationContext ctx, Serializer.Struct struct, R value) {
                StructEndec.this.encodeStruct(ctx, struct, from.apply(value));
            }
            @Override
            public R decodeStruct(SerializationContext ctx, Deserializer.Struct struct) {
                return to.apply(StructEndec.this.decodeStruct(ctx, struct));
            }
        };
    }

    default MapCodec<T> mapCodec(SerializationContext assumedContext) {
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
                                ops.getStringValue(pair.getFirst())
                                        .getOrThrow(s -> new IllegalStateException("Unable to parse key: " + s)),
                                ops.convertTo(EdmOps.withoutContext(), pair.getSecond())
                        );
                    });

                    var rootOps = ops;
                    while (rootOps instanceof ForwardingDynamicOps<T1>) rootOps = ((ForwardingDynamicOpsAccessor<T1>) ops).owo$delegate();

                    var context = rootOps instanceof EdmOps edmOps
                            ? edmOps.capturedContext().and(assumedContext)
                            : assumedContext;

                    if (ops instanceof RegistryOps<T1> registryOps) {
                        context = context.withAttributes(RegistriesAttribute.infoGetterOnly(((RegistryOpsAccessor) registryOps).owo$infoGetter()));
                    }

                    return DataResult.success(StructEndec.this.decode(context, LenientEdmDeserializer.of(EdmElement.wrapMap(map))));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }

            @Override
            public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
                try {
                    var rootOps = ops;
                    while (rootOps instanceof ForwardingDynamicOps<T1>) rootOps = ((ForwardingDynamicOpsAccessor<T1>) ops).owo$delegate();

                    var context = rootOps instanceof EdmOps edmOps
                            ? edmOps.capturedContext().and(assumedContext)
                            : assumedContext;

                    if (ops instanceof RegistryOps<T1> registryOps) {
                        context = context.withAttributes(RegistriesAttribute.infoGetterOnly(((RegistryOpsAccessor) registryOps).owo$infoGetter()));
                    }

                    var element = StructEndec.this.encodeFully(context, EdmSerializer::of, input).<Map<String, EdmElement<?>>>cast();

                    var result = prefix;
                    for (var entry : element.entrySet()) {
                        result = result.add(entry.getKey(), EdmOps.withoutContext().convertTo(ops, entry.getValue()));
                    }

                    return result;
                } catch (Exception e) {
                    return prefix.withErrorsFrom(DataResult.error(e::getMessage, input));
                }
            }
        };
    }

    default MapCodec<T> mapCodec() {
        return this.mapCodec(SerializationContext.empty());
    }
}
