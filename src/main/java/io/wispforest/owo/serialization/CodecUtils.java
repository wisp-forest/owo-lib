package io.wispforest.owo.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.wispforest.endec.*;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.endec.format.edm.*;
import io.wispforest.endec.format.forwarding.ForwardingDeserializer;
import io.wispforest.endec.format.forwarding.ForwardingSerializer;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.format.gson.GsonEndec;
import io.wispforest.endec.format.gson.GsonSerializer;
import io.wispforest.owo.mixin.ForwardingDynamicOpsAccessor;
import io.wispforest.owo.mixin.RegistryOpsAccessor;
import io.wispforest.owo.serialization.endec.EitherEndec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.serialization.format.ContextHolder;
import io.wispforest.owo.serialization.format.ContextedDelegatingOps;
import io.wispforest.owo.serialization.format.edm.EdmOps;
import io.wispforest.owo.serialization.format.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.format.nbt.NbtEndec;
import io.wispforest.owo.serialization.format.nbt.NbtSerializer;
import io.wispforest.owo.util.Scary;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.dynamic.ForwardingDynamicOps;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked"})
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
        return Endec.of(encoderOfCodec(codec), decoderOfCodec(codec));
    }

    private static <T> Endec.Encoder<T> encoderOfCodec(Codec<T> codec) {
        return (ctx, serializer, value) -> {
            var unpackedSerializer = unpackSerializer(serializer);
            var pair = (Pair<DynamicOps, CodecInteropBinding>) (Object) convertToOps(unpackedSerializer, ctx);

            if(pair != null) {
                setDecodedValueUnsafe(pair.getSecond(), unpackedSerializer, codec.encodeStart(pair.getFirst(), value).getOrThrow());
            } else {
                EdmEndec.INSTANCE.encode(ctx, serializer, codec.encodeStart(createEdmOps(ctx), value).getOrThrow());
            }
        };
    }

    private static <T> Endec.Decoder<T> decoderOfCodec(Codec<T> codec) {
        return (ctx, deserializer) -> {
            var unpackedDeserializer = unpackDeserializer(deserializer);
            var pair = (Pair<DynamicOps, CodecInteropBinding>) (Object) CodecUtils.convertToOps(unpackedDeserializer, ctx);

            return (pair != null)
                    ? codec.parse((DynamicOps<Object>) pair.getFirst(), getEncodedValueUnsafe(pair.getSecond(), unpackedDeserializer)).getOrThrow()
                    : codec.parse(createEdmOps(ctx), EdmEndec.INSTANCE.decode(ctx, deserializer)).getOrThrow();
        };
    }

    public static <T> Endec<T> toEndec(Codec<T> codec, PacketCodec<ByteBuf, T> packetCodec) {
        var encoder = encoderOfCodec(codec);
        var decoder = decoderOfCodec(codec);

        return Endec.of(
                (ctx, serializer, value) -> {
                    if (serializer instanceof ByteBufSerializer<?>) {
                        var buffer = new PacketByteBuf(Unpooled.buffer());

                        packetCodec.encode(buffer, value);

                        MinecraftEndecs.PACKET_BYTE_BUF.encode(ctx, serializer, buffer);
                    } else {
                        encoder.encode(ctx, serializer, value);
                    }
                },
                (ctx, deserializer) -> {
                    if (deserializer instanceof ByteBufDeserializer) {
                        return packetCodec.decode(MinecraftEndecs.PACKET_BYTE_BUF.decode(ctx, deserializer));
                    } else {
                        return decoder.decode(ctx, deserializer);
                    }
                }
        );
    }

    public static <T> Endec<T> toEndecWithRegistries(Codec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec) {
        var encoder = encoderOfCodec(codec);
        var decoder = decoderOfCodec(codec);

        return Endec.of(
                (ctx, serializer, value) -> {
                    if (serializer instanceof ByteBufSerializer<?>) {
                        var buffer = new RegistryByteBuf(new PacketByteBuf(Unpooled.buffer()), ctx.requireAttributeValue(RegistriesAttribute.REGISTRIES).registryManager());

                        packetCodec.encode(buffer, value);

                        MinecraftEndecs.PACKET_BYTE_BUF.encode(ctx, serializer, buffer);
                    } else {
                        encoder.encode(ctx, serializer, value);
                    }
                },
                (ctx, deserializer) -> {
                    if (deserializer instanceof ByteBufDeserializer) {
                        return packetCodec.decode(
                                new RegistryByteBuf(
                                        MinecraftEndecs.PACKET_BYTE_BUF.decode(ctx, deserializer),
                                        ctx.requireAttributeValue(RegistriesAttribute.REGISTRIES).registryManager()
                                ));
                    } else {
                        return decoder.decode(ctx, deserializer);
                    }
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
                return captureThrows(() -> {
                    var deserializer = convertToDeserializer(ops, input);

                    var context = createContext(ops, assumedContext);

                    T decodedValue = (deserializer != null)
                            ? endec.decode(deserializer.setupContext(context), deserializer)
                            : endec.decode(context, LenientEdmDeserializer.of(ops.convertTo(EdmOps.withoutContext(), input)));

                    return new Pair<>(decodedValue, input);
                });
            }

            @Override
            public <D> DataResult<D> encode(T input, DynamicOps<D> ops, D prefix) {
                return captureThrows(() -> {
                    var serializer = convertToSerializer(ops);

                    var context = createContext(ops, assumedContext);

                    return (serializer != null)
                            ? endec.encodeFully(context, () -> serializer, input)
                            : EdmOps.withoutContext().convertTo(ops, endec.encodeFully(context, EdmSerializer::of, input));
                });
            }
        };
    }

    @Deprecated
    public static <T> Codec<T> ofEndec(Endec<T> endec) {
        return toCodec(endec);
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
                return captureThrows(() -> {
                    var deserializer = convertToDeserializerStruct(ops, input);

                    var context = createContext(ops, assumedContext);

                    if(deserializer != null) {
                        return structEndec.decode(deserializer.setupContext(context), deserializer);
                    } else {
                        var map = new HashMap<String, EdmElement<?>>();

                        input.entries().forEach(pair -> {
                            map.put(
                                    ops.getStringValue(pair.getFirst())
                                            .getOrThrow(s -> new IllegalStateException("Unable to parse key: " + s)),
                                    ops.convertTo(EdmOps.withoutContext(), pair.getSecond())
                            );
                        });

                        return structEndec.decode(context, LenientEdmDeserializer.of(EdmElement.wrapMap(map)));
                    }
                });
            }

            @Override
            public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
                try {
                    var context = createContext(ops, assumedContext);

                    var pair = convertToSerializerStruct(ops, prefix);

                    if(pair != null) {
                        var serializer = pair.getFirst();

                        return pair.getSecond().apply(structEndec.encodeFully(serializer.setupContext(context), () -> serializer, input));
                    } else {
                        var element = structEndec.encodeFully(context, EdmSerializer::of, input).<Map<String, EdmElement<?>>>cast();

                        var result = prefix;
                        for (var entry : element.entrySet()) {
                            result = result.add(entry.getKey(), EdmOps.withoutContext().convertTo(ops, entry.getValue()));
                        }

                        return result;
                    }
                } catch (Exception e) {
                    return prefix.withErrorsFrom(DataResult.error(e::getMessage, input));
                }
            }
        };
    }

    public static <T> MapCodec<T> toMapCodec(StructEndec<T> structEndec) {
        return toMapCodec(structEndec, SerializationContext.empty());
    }

    /*
     * This method overall should be fine but do not expect such to work always as it could be a problem as
     * it bypasses certain features about Deserializer API that may be an issue but is low chance for general
     * cases within Minecraft.
     *
     * blodhgarm: 21.07.2024
     */
    @Scary
    @ApiStatus.Experimental
    public static <T> StructEndec<T> toStructEndec(MapCodec<T> mapCodec) {
        return new StructEndec<T>() {
            @Override
            public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, T value) {
                var unpackedSerializer = unpackSerializer(serializer);
                var pair = (Pair<DynamicOps, CodecInteropBinding>) (Object) convertToOps(unpackedSerializer, ctx);

                if(pair != null) {
                    setEncodedValueStruct(pair.getSecond(), pair.getFirst(), unpackedSerializer, struct, mapCodec, value);
                } else {
                    var edmOps = createEdmOps(ctx);

                    var edmMap = mapCodec.encode(value, edmOps, edmOps.mapBuilder()).build(edmOps.emptyMap())
                            .getOrThrow()
                            .asMap();

                    if (serializer instanceof SelfDescribedSerializer<?>) {
                        edmMap.value().forEach((s, element) -> struct.field(s, ctx, EdmEndec.INSTANCE, element));
                    } else {
                        struct.field("element", ctx, EdmEndec.MAP, edmMap);
                    }
                }
            }

            @Override
            public T decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
                var unpackepDeserializer = unpackDeserializer(deserializer);
                var pair = (Pair<DynamicOps, CodecInteropBinding>) (Object) convertToOps(unpackepDeserializer, ctx);

                if(pair != null) {
                    return (T) getEncodedValueStruct(pair.getSecond(), pair.getFirst(), unpackepDeserializer, struct, mapCodec);
                } else {
                    var edmMap = ((deserializer instanceof SelfDescribedDeserializer<?>)
                            ? EdmEndec.MAP.decode(ctx, deserializer)
                            : struct.field("element", ctx, EdmEndec.MAP));

                    var ops = createEdmOps(ctx);

                    return mapCodec.decode(ops, ops.getMap(edmMap).getOrThrow()).getOrThrow();
                }
            }
        };
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

    //--

    private static SerializationContext createContext(DynamicOps<?> ops, SerializationContext assumedContext) {
        var rootOps = ops;

        var context = rootOps instanceof ContextHolder holder ? holder.capturedContext().and(assumedContext) : null;

        while (rootOps instanceof ForwardingDynamicOps<?>) {
            rootOps = ((ForwardingDynamicOpsAccessor<?>) rootOps).owo$delegate();

            if(context == null && rootOps instanceof ContextHolder holder) {
                context = holder.capturedContext().and(assumedContext);
            }
        }

        if(context == null) context = assumedContext;

        if (ops instanceof RegistryOps<?> registryOps) {
            context = context.withAttributes(RegistriesAttribute.tryFromCachedInfoGetter(((RegistryOpsAccessor) registryOps).owo$infoGetter()));
        }

        return context;
    }

    private static DynamicOps<EdmElement<?>> createEdmOps(SerializationContext ctx) {
        DynamicOps<EdmElement<?>> ops = EdmOps.withContext(ctx);

        if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
            ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
        }

        return ops;
    }

    private static <T> DataResult<T> captureThrows(Supplier<T> action) {
        try {
            return DataResult.success(action.get());
        } catch (Exception e) {
            return DataResult.error(e::getMessage);
        }
    }

    //--

    private static final Map<Class<? extends Serializer<?>>, CodecInteropBinding<?, ?, ?>> serializerToBinding = new HashMap<>();
    private static final Map<Class<? extends Deserializer<?>>, CodecInteropBinding<?, ?, ?>> deserializerToBinding = new HashMap<>();
    private static final Map<Class<? extends DynamicOps<?>>, CodecInteropBinding<?, ?, ?>> opsToBinding = new HashMap<>();

    public static void registerInteropBinding(CodecInteropBinding<?, ?, ?> binding) {
        if (serializerToBinding.containsKey(binding.serializerClass())) {
            throw new IllegalStateException("Unable to add the given CodecInteropBinding as the given Serializer clazz was already added by another! [Class: " + binding.serializerClass() + "]");
        }
        if (deserializerToBinding.containsKey(binding.deserializerClass())) {
            throw new IllegalStateException("Unable to add the given CodecInteropBinding as the given Deserializer clazz was already added by another! [Class: " + binding.deserializerClass() + "]");
        }
        if (opsToBinding.containsKey(binding.opsClass())) {
            throw new IllegalStateException("Unable to add the given CodecInteropBinding as the given DynamicOps clazz was already added by another! [Class: " + binding.opsClass() + "]");
        }

        serializerToBinding.put(binding.serializerClass(), binding);
        deserializerToBinding.put(binding.deserializerClass(), binding);
        opsToBinding.put(binding.opsClass(), binding);
    }

    private static <T> DynamicOps<T> unpackOps(DynamicOps<T> ops) {
        var rootOps = ops;
        while (rootOps instanceof ForwardingDynamicOps<T>) rootOps = ((ForwardingDynamicOpsAccessor<T>) rootOps).owo$delegate();
        return rootOps;
    }

    private static <T> Serializer<T> unpackSerializer(Serializer<T> serializer) {
        var rootSerializer = serializer;
        while (rootSerializer instanceof ForwardingSerializer<T> forwardingSerializer) rootSerializer = forwardingSerializer.delegate();
        return rootSerializer;
    }

    private static <T> Deserializer<T> unpackDeserializer(Deserializer<T> deserializer) {
        var rootDeserializer = deserializer;
        while (rootDeserializer instanceof ForwardingDeserializer<T> forwardingDeserializer) rootDeserializer = forwardingDeserializer.delegate();
        return rootDeserializer;
    }

    @Nullable
    private static <T, S extends Serializer<T>> Pair<DynamicOps<T>, CodecInteropBinding<T, S, ?>> convertToOps(Serializer<T> serializer, SerializationContext ctx) {
        var bindings = (CodecInteropBinding<T, S, ?>) serializerToBinding.get(serializer.getClass());

        if(bindings != null) {
            DynamicOps<T> ops = ContextedDelegatingOps.withContext(ctx, bindings.getOps());

            if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
                ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
            }

            return new Pair<>(ops, bindings);
        }

        return null;
    }

    @Nullable
    private static <T, D extends Deserializer<T>> Pair<DynamicOps<T>, CodecInteropBinding<T, ?, D>> convertToOps(Deserializer<T> deserializer, SerializationContext ctx) {
        var bindings = (CodecInteropBinding<T, ?, D>) deserializerToBinding.get(deserializer.getClass());

        if (bindings != null) {
            DynamicOps<T> ops = ContextedDelegatingOps.withContext(ctx, (bindings).getOps());

            if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
                ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
            }

            return new Pair<>(ops, bindings);
        }

        return null;
    }

    @Nullable
    private static <T> Deserializer<T> convertToDeserializer(DynamicOps<T> dynamicOps, T t) {
        var bindings = (CodecInteropBinding<T, ?, Deserializer<T>>) opsToBinding.get(unpackOps(dynamicOps).getClass());

        return (bindings != null) ? bindings.createDeserializer(t) : null;
    }

    @Nullable
    private static <T> Deserializer<T> convertToDeserializerStruct(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
        var bindings = (CodecInteropBinding<T, ?, Deserializer<T>>) opsToBinding.get(unpackOps(dynamicOps).getClass());

        return (bindings != null)
                ? bindings.createDeserializer(bindings.convertMapLike(mapLike))
                : null;
    }

    @Nullable
    private static <T> Pair<Serializer<T>, Function<T, RecordBuilder<T>>> convertToSerializerStruct(DynamicOps<T> dynamicOps, RecordBuilder<T> builder) {
        var bindings = (CodecInteropBinding<T, Serializer<T>, ?>) opsToBinding.get(unpackOps(dynamicOps).getClass());

        return (bindings != null)
                ? new Pair<>(bindings.createSerializer(), t -> bindings.addToBuilder(t, builder))
                : null;
    }

    @Nullable
    private static <T> Serializer<T> convertToSerializer(DynamicOps<T> dynamicOps) {
        var bindings = (CodecInteropBinding<T, Serializer<T>, ?>) opsToBinding.get(unpackOps(dynamicOps).getClass());

        return (bindings != null) ? bindings.createSerializer() : null;
    }

    //--

    private static <T, S extends Serializer<T>> void setDecodedValueUnsafe(CodecInteropBinding<T, S, ?> binding, S serializer, Object t) {
        binding.setEncodedValue(serializer, (T) t);
    }

    private static <T, D extends Deserializer<T>> T getEncodedValueUnsafe(CodecInteropBinding<T, ?, D> binding, D deserializer) {
        return binding.getEncodedValue(deserializer);
    }

    private static <T, V, S extends Serializer<T>> void setEncodedValueStruct(CodecInteropBinding<T, S, ?> binding, DynamicOps<T> ops, S serializer, Serializer.Struct struct, MapCodec<V> mapCodec, V v) {
        var t = mapCodec.encode(v, ops, ops.mapBuilder()).build(ops.emptyMap()).getOrThrow();

        binding.setEncodedValueStruct(SerializationContext.empty(), serializer, struct, t);
    }

    private static <T, V, D extends Deserializer<T>> V getEncodedValueStruct(CodecInteropBinding<T, ?, D> binding, DynamicOps<T> ops, D deserializer, Deserializer.Struct struct, MapCodec<V> mapCodec) {
        var t = binding.getEncodedValueStruct(SerializationContext.empty(), deserializer, struct);

        return mapCodec.decode(ops, ops.getMap(t).getOrThrow()).getOrThrow();
    }

    public interface CodecInteropBinding<T, S extends Serializer<T>, D extends Deserializer<T>> {
        Class<? extends Serializer<T>> serializerClass();
        Class<? extends Deserializer<T>> deserializerClass();
        Class<? extends DynamicOps<T>> opsClass();

        //--

        S createSerializer();
        D createDeserializer(T t);
        DynamicOps<T> getOps();

        //-

        T convertMapLike(MapLike<T> mapLike);
        RecordBuilder<T> addToBuilder(T t, RecordBuilder<T> builder);

        //--

        void setEncodedValue(S serializer, T t);
        T getEncodedValue(D deserializer);

        //--

        void setEncodedValueStruct(SerializationContext ctx, S serializer, Serializer.Struct struct, T t);
        T getEncodedValueStruct(SerializationContext ctx, D serializer, Deserializer.Struct struct);
    }

    static {
        registerInteropBinding(new CodecInteropBinding<NbtElement, NbtSerializer, NbtDeserializer>() {
            @Override
            public Class<? extends Serializer<NbtElement>> serializerClass() {
                return NbtSerializer.class;
            }

            @Override
            public Class<? extends Deserializer<NbtElement>> deserializerClass() {
                return NbtDeserializer.class;
            }

            @Override
            public Class<? extends DynamicOps<NbtElement>> opsClass() {
                return NbtOps.class;
            }

            @Override
            public NbtSerializer createSerializer() {
                return NbtSerializer.of();
            }

            @Override
            public NbtDeserializer createDeserializer(NbtElement tag) {
                return NbtDeserializer.of(tag);
            }

            @Override
            public DynamicOps<NbtElement> getOps() {
                return NbtOps.INSTANCE;
            }

            @Override
            public NbtElement convertMapLike(MapLike<NbtElement> mapLike) {
                var compound = new NbtCompound();

                mapLike.entries().forEach(pairs -> {
                    var key = pairs.getFirst();
                    var value = pairs.getSecond();

                    if(!(key instanceof NbtString primitive)) throw new IllegalStateException("Unable to parse key: " + key);

                    compound.put(primitive.asString(), value);
                });

                return compound;
            }

            @Override
            public RecordBuilder<NbtElement> addToBuilder(NbtElement tag, RecordBuilder<NbtElement> builder) {
                if(!(tag instanceof NbtCompound compoundTag)) {
                    throw new IllegalStateException("Unable to add to builder as the given Tag was not a CompoundTag: " + tag);
                }

                var result = builder;

                for (var key : compoundTag.getKeys()) result = result.add(key, compoundTag.get(key));

                return result;
            }

            @Override
            public void setEncodedValue(NbtSerializer serializer, NbtElement tag) {
                createDeserializer(tag).readAny(SerializationContext.empty(), serializer);
            }

            @Override
            public NbtElement getEncodedValue(NbtDeserializer deserializer) {
                var serializer = createSerializer();

                deserializer.readAny(SerializationContext.empty(), serializer);

                return serializer.result();
            }

            @Override
            public void setEncodedValueStruct(SerializationContext ctx, NbtSerializer serializer, Serializer.Struct struct, NbtElement tag) {
                if(!(tag instanceof NbtCompound compoundTag)) {
                    throw new IllegalStateException("Unable to add to builder as the given Tag was not a CompoundTag: " + tag);
                }

                compoundTag.getKeys().forEach(key -> struct.field(key, ctx, NbtEndec.ELEMENT, compoundTag.get(key)));
            }

            @Override
            public NbtElement getEncodedValueStruct(SerializationContext ctx, NbtDeserializer deserializer, Deserializer.Struct struct) {
                return NbtEndec.COMPOUND.decode(ctx, deserializer);
            }
        });

        registerInteropBinding(new CodecInteropBinding<JsonElement, GsonSerializer, GsonDeserializer>() {
            @Override
            public Class<? extends Serializer<JsonElement>> serializerClass() {
                return GsonSerializer.class;
            }

            @Override
            public Class<? extends Deserializer<JsonElement>> deserializerClass() {
                return GsonDeserializer.class;
            }

            @Override
            public Class<? extends DynamicOps<JsonElement>> opsClass() {
                return JsonOps.class;
            }

            @Override
            public GsonSerializer createSerializer() {
                return GsonSerializer.of();
            }

            @Override
            public GsonDeserializer createDeserializer(JsonElement jsonElement) {
                return GsonDeserializer.of(jsonElement);
            }

            @Override
            public DynamicOps<JsonElement> getOps() {
                return JsonOps.INSTANCE;
            }

            @Override
            public JsonElement convertMapLike(MapLike<JsonElement> mapLike) {
                var jsonObject = new JsonObject();

                mapLike.entries().forEach(pairs -> {
                    var key = pairs.getFirst();
                    var value = pairs.getSecond();

                    if(!(key instanceof JsonPrimitive primitive && primitive.isString())) throw new IllegalStateException("Unable to parse key: " + key);

                    jsonObject.add(primitive.getAsString(), value);
                });

                return jsonObject;
            }

            @Override
            public RecordBuilder<JsonElement> addToBuilder(JsonElement jsonElement, RecordBuilder<JsonElement> builder) {
                if(!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new IllegalStateException("Unable to add to builder as the given JsonElement was not a JsonObject: " + jsonElement);
                }

                var result = builder;

                for (var entry : jsonObject.asMap().entrySet()) result = result.add(entry.getKey(), entry.getValue());

                return result;
            }

            @Override
            public void setEncodedValue(GsonSerializer serializer, JsonElement tag) {
                createDeserializer(tag).readAny(SerializationContext.empty(), serializer);
            }

            @Override
            public JsonElement getEncodedValue(GsonDeserializer deserializer) {
                var serializer = createSerializer();

                deserializer.readAny(SerializationContext.empty(), serializer);

                return serializer.result();
            }

            @Override
            public void setEncodedValueStruct(SerializationContext ctx, GsonSerializer serializer, Serializer.Struct struct, JsonElement jsonElement) {
                if(!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new IllegalStateException("Unable to add to builder as the given JsonElement was not a JsonObject: " + jsonElement);
                }

                jsonObject.asMap().forEach((key, element) ->  struct.field(key, ctx, GsonEndec.INSTANCE, element));
            }

            @Override
            public JsonElement getEncodedValueStruct(SerializationContext ctx, GsonDeserializer serializer, Deserializer.Struct struct) {
                return GsonEndec.INSTANCE.decode(ctx, serializer);
            }
        });
    }
}