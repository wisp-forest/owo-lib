package io.wispforest.owo.serialization;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import io.netty.buffer.ByteBuf;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.endec.format.edm.*;
import io.wispforest.owo.mixin.ForwardingDynamicOpsAccessor;
import io.wispforest.owo.mixin.RegistryOpsAccessor;
import io.wispforest.owo.serialization.endec.EitherEndec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.serialization.format.edm.EdmOps;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.dynamic.ForwardingDynamicOps;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CodecUtils {

    /**
     * Create a new endec serializing the same data as {@code codec}
     * <p>
     * This method is implemented by converting all data to be (de-)serialized
     * to the Endec Data Model data format (hereto-forth to be referred to as EDM)
     * which has both an endec ({@link EdmEndec}) and DynamicOps implementation ({@link EdmOps}).
     * Since EDM encodes structure using a self-described format's native structural types,
     * <b>this means that for JSON and NBT, the created endec's serialized representation is identical
     * to that of {@code codec}</b>. In general, for non-self-described formats, the serialized
     * representation is a byte array
     * <p>
     * When decoding, an EDM element is read from the deserializer and then parsed using {@code codec}
     * <p>
     * When encoding, the value is encoded using {@code codec} to an EDM element which is then
     * written into the serializer
     */
    public static <T> Endec<T> toEndec(Codec<T> codec) {
        return Endec.of(
                (ctx, serializer, value) -> {
                    DynamicOps<EdmElement<?>> ops = EdmOps.withContext(ctx);
                    if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
                        ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
                    }

                    EdmEndec.INSTANCE.encode(ctx, serializer, codec.encodeStart(ops, value).getOrThrow(IllegalStateException::new));
                },
                (ctx, deserializer) -> {
                    DynamicOps<EdmElement<?>> ops = EdmOps.withContext(ctx);
                    if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
                        ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
                    }

                    return codec.parse(ops, EdmEndec.INSTANCE.decode(ctx, deserializer)).getOrThrow(IllegalStateException::new);
                }
        );
    }

    public static <T> Endec<T> toEndec(Codec<T> codec, PacketCodec<ByteBuf, T> packetCodec) {
        return Endec.of(
                (ctx, serializer, value) -> {
                    if (serializer instanceof ByteBufSerializer<?>) {
                        var buffer = PacketByteBufs.create();
                        packetCodec.encode(buffer, value);

                        MinecraftEndecs.PACKET_BYTE_BUF.encode(ctx, serializer, buffer);
                        return;
                    }

                    DynamicOps<EdmElement<?>> ops = EdmOps.withContext(ctx);
                    if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
                        ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
                    }

                    EdmEndec.INSTANCE.encode(ctx, serializer, codec.encodeStart(ops, value).getOrThrow(IllegalStateException::new));
                },
                (ctx, deserializer) -> {
                    if (deserializer instanceof ByteBufDeserializer) {
                        var buffer = MinecraftEndecs.PACKET_BYTE_BUF.decode(ctx, deserializer);
                        return packetCodec.decode(buffer);
                    }

                    DynamicOps<EdmElement<?>> ops = EdmOps.withContext(ctx);
                    if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
                        ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
                    }

                    return codec.parse(ops, EdmEndec.INSTANCE.decode(ctx, deserializer)).getOrThrow(IllegalStateException::new);
                }
        );
    }

    /**
     * Create an endec which serializes an instance of {@link Either}, using {@code first}
     * for the left and {@code second} for the right variant
     * <p>
     * In a self-describing format, the serialized representation is simply that of the endec of
     * whichever variant is represented. In the general for non-self-described formats, the
     * which variant is represented must also be stored
     */
    public static <F, S> Endec<Either<F, S>> eitherEndec(Endec<F> first, Endec<S> second) {
        return new EitherEndec<>(first, second, false);
    }

    /**
     * Like {@link #eitherEndec(Endec, Endec)}, but ensures when decoding from a self-described format
     * that only {@code first} or {@code second}, but not both, succeed
     */
    public static <F, S> Endec<Either<F, S>> xorEndec(Endec<F> first, Endec<S> second) {
        return new EitherEndec<>(first, second, true);
    }

    //--

    /**
     * Create a codec serializing the same data as this endec, assuming
     * that the serialized format posses the {@code assumedAttributes}
     * <p>
     * This method is implemented by converting between a given DynamicOps'
     * datatype and EDM (see {@link #toEndec(Codec)}) and then encoding/decoding
     * from/to an EDM element using the {@link EdmSerializer} and {@link EdmDeserializer}
     * <p>
     * The serialized representation of a codec created through this method is generally
     * identical to that of a codec manually created to describe the same data
     */
    public static <T> Codec<T> toCodec(Endec<T> endec, SerializationContext assumedContext) {
        return new Codec<>() {
            @Override
            public <D> DataResult<Pair<T, D>> decode(DynamicOps<D> ops, D input) {
                try {
                    var rootOps = ops;
                    while (rootOps instanceof ForwardingDynamicOps<D>) rootOps = ((ForwardingDynamicOpsAccessor<D>) rootOps).owo$delegate();

                    var context = rootOps instanceof EdmOps edmOps
                            ? edmOps.capturedContext().and(assumedContext)
                            : assumedContext;

                    if (ops instanceof RegistryOps<D> registryOps) {
                        context = context.withAttributes(RegistriesAttribute.infoGetterOnly(((RegistryOpsAccessor) registryOps).owo$infoGetter()));
                    }

                    return DataResult.success(new Pair<>(endec.decode(context, LenientEdmDeserializer.of(ops.convertTo(EdmOps.withoutContext(), input))), input));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public <D> DataResult<D> encode(T input, DynamicOps<D> ops, D prefix) {
                try {
                    var rootOps = ops;
                    while (rootOps instanceof ForwardingDynamicOps<D>) rootOps = ((ForwardingDynamicOpsAccessor<D>) rootOps).owo$delegate();

                    var context = rootOps instanceof EdmOps edmOps
                            ? edmOps.capturedContext().and(assumedContext)
                            : assumedContext;

                    if (ops instanceof RegistryOps<D> registryOps) {
                        context = context.withAttributes(RegistriesAttribute.infoGetterOnly(((RegistryOpsAccessor) registryOps).owo$infoGetter()));
                    }

                    return DataResult.success(EdmOps.withoutContext().convertTo(ops, endec.encodeFully(context, EdmSerializer::of, input)));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }
        };
    }

    public static <T> Codec<T> toCodec(Endec<T> endec) {
        return toCodec(endec, SerializationContext.empty());
    }

    public static <T> MapCodec<T> toMapCodec(StructEndec<T> structEndec, SerializationContext assumedContext) {
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

                    return DataResult.success(structEndec.decode(context, LenientEdmDeserializer.of(EdmElement.wrapMap(map))));
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

                    var element = structEndec.encodeFully(context, EdmSerializer::of, input).<Map<String, EdmElement<?>>>cast();

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

    public static <T> MapCodec<T> toMapCodec(StructEndec<T> structEndec) {
        return toMapCodec(structEndec, SerializationContext.empty());
    }

    // the fact that we lose context here is certainly far from ideal,
    // but for the most part *shouldn't* matter. after all, ideally nobody
    // should ever be nesting packet codecs into endecs - there's little
    // point to doing that and transferring any kind of context data becomes
    // mostly impossible because the system turns into one opaque spaghetti mess
    //
    // glisco, 28.04.2024
    public static <B extends PacketByteBuf, T> PacketCodec<B, T> toPacketCodec(Endec<T> endec) {
        return new PacketCodec<>() {
            @Override
            public T decode(B buf) {
                var ctx = buf instanceof RegistryByteBuf registryByteBuf
                        ? SerializationContext.attributes(RegistriesAttribute.of(registryByteBuf.getRegistryManager()))
                        : SerializationContext.empty();

                return endec.decode(ctx, ByteBufDeserializer.of(buf));
            }

            @Override
            public void encode(B buf, T value) {
                var ctx = buf instanceof RegistryByteBuf registryByteBuf
                        ? SerializationContext.attributes(RegistriesAttribute.of(registryByteBuf.getRegistryManager()))
                        : SerializationContext.empty();

                endec.encode(ctx, ByteBufSerializer.of(buf), value);
            }
        };
    }
}
