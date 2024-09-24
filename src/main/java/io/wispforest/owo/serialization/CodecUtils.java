package io.wispforest.owo.serialization;

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
import java.util.stream.Collector;
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
        return Endec.of(encoderOfCodec(codec), decoderOfCodec(codec));
    }

    private static <T> Endec.Encoder<T> encoderOfCodec(Codec<T> codec) {
        return (ctx, serializer, value) -> {
            var pair = convertToOps(serializer, ctx);

            if(pair != null) {
                setDecodedValueUnsafe(pair.getSecond(), serializer, codec.encodeStart(pair.getFirst(), value).getOrThrow());
            } else {
                EdmEndec.INSTANCE.encode(ctx, serializer, codec.encodeStart(createEdmOps(ctx), value).getOrThrow());
            }
        };
    }

    private static <T> Endec.Decoder<T> decoderOfCodec(Codec<T> codec) {
        return (ctx, deserializer) -> {
            var pair = CodecUtils.convertToOps(deserializer, ctx);

            return (pair != null)
                    ? codec.parse((DynamicOps<Object>) pair.getFirst(), getEncodedValueUnsafe(pair.getSecond(), deserializer)).getOrThrow()
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
                var pair = convertToOps(serializer, ctx);

                if(pair != null) {
                    setEncodedValueStructUnsafe(pair.getSecond(), pair.getFirst(), serializer, struct, mapCodec, value);
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
                var pair = convertToOps(deserializer, ctx);

                if(pair != null) {
                    return getEncodedValueStructUnsafe(pair.getSecond(), pair.getFirst(), deserializer, struct, mapCodec);
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

    private static final Map<Class<? extends Serializer<?>>, CodecInteropBinding<?>> serializerToBinding = new HashMap<>();
    private static final Map<Class<? extends Deserializer<?>>, CodecInteropBinding<?>> deserializerToBinding = new HashMap<>();
    private static final Map<Class<? extends DynamicOps<?>>, CodecInteropBinding<?>> opsToBinding = new HashMap<>();

    public static <T> void registerInteropBinding(CodecInteropBinding<T> binding) {
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
    private static <T> Pair<DynamicOps<T>, CodecInteropBinding<T>> convertToOps(Serializer<T> serializer, SerializationContext ctx) {
        var bindings = serializerToBinding.get(unpackSerializer(serializer).getClass());

        if(bindings != null) {
            DynamicOps<T> ops = ContextedDelegatingOps.withContext(ctx, ((CodecInteropBinding<T>) bindings).getOps());

            if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
                ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
            }

            return new Pair<>(ops, (CodecInteropBinding<T>) bindings);
        }

        return null;
    }

    @Nullable
    private static <T> Pair<DynamicOps<T>, CodecInteropBinding<T>> convertToOps(Deserializer<T> deserializer, SerializationContext ctx) {
        var bindings = deserializerToBinding.get(unpackDeserializer(deserializer).getClass());

        if (bindings != null) {
            DynamicOps<T> ops = ContextedDelegatingOps.withContext(ctx, ((CodecInteropBinding<T>) bindings).getOps());

            if (ctx.hasAttribute(RegistriesAttribute.REGISTRIES)) {
                ops = RegistryOps.of(ops, ctx.getAttributeValue(RegistriesAttribute.REGISTRIES).infoGetter());
            }

            return new Pair<>(ops, (CodecInteropBinding<T>) bindings);
        }

        return null;
    }

    @Nullable
    private static <T> Deserializer<T> convertToDeserializer(DynamicOps<T> dynamicOps, T t) {
        var bindings = opsToBinding.get(unpackOps(dynamicOps).getClass());

        return (bindings != null) ? ((CodecInteropBinding<T>) bindings).createDeserializer(t) : null;
    }

    @Nullable
    private static <T> Deserializer<T> convertToDeserializerStruct(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
        var bindings = opsToBinding.get(unpackOps(dynamicOps).getClass());

        return (bindings != null)
                ? ((CodecInteropBinding<T>) bindings).createDeserializer(((CodecInteropBinding<T>) bindings).convertMapLike(mapLike))
                : null;
    }

    @Nullable
    private static <T> Pair<Serializer<T>, Function<T, RecordBuilder<T>>> convertToSerializerStruct(DynamicOps<T> dynamicOps, RecordBuilder<T> builder) {
        var bindings = opsToBinding.get(unpackOps(dynamicOps).getClass());

        return (bindings != null)
                ? new Pair<>(((CodecInteropBinding<T>) bindings).createSerializer(), t -> ((CodecInteropBinding<T>) bindings).addToBuilder(t, builder))
                : null;
    }

    @Nullable
    private static <T> Serializer<T> convertToSerializer(DynamicOps<T> dynamicOps) {
        var bindings = opsToBinding.get(unpackOps(dynamicOps).getClass());

        return (bindings != null) ? ((CodecInteropBinding<T>) bindings).createSerializer() : null;
    }

    //--

    private static <T> void setDecodedValueUnsafe(CodecInteropBinding<T> binding, Serializer<?> serializer, Object t) {
        try {
            binding.setEncodedValue((Serializer<T>) serializer, (T) t);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Unable to set the given encoded value into the passed Serializer as its not the correct type!", e);
        }
    }

    private static <T> T getEncodedValueUnsafe(CodecInteropBinding<T> binding, Deserializer<?> deserializer) {
        try {
            return binding.getEncodedValue((Deserializer<T>) deserializer);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Unable to get the given encoded value from the passed Deserializer as its not the correct type!", e);
        }
    }

    private static <T, V> void setEncodedValueStructUnsafe(CodecInteropBinding<T> binding, DynamicOps<?> ops, Serializer<?> serializer, Serializer.Struct struct, MapCodec<V> mapCodec, V v) {
        try {
            var typedOps = (DynamicOps<T>) ops;
            var t = mapCodec.encode(v, typedOps, typedOps.mapBuilder()).build(typedOps.emptyMap()).getOrThrow();

            binding.setEncodedValueStruct(SerializationContext.empty(), serializer, struct, t);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Unable to set the given encoded value into the passed Struct Serializer as its not the correct type!", e);
        }
    }

    private static <T, V> V getEncodedValueStructUnsafe(CodecInteropBinding<T> binding, DynamicOps<?> ops, Deserializer<?> deserializer, Deserializer.Struct struct, MapCodec<V> mapCodec) {
        try {
            var typedOps = (DynamicOps<T>) ops;
            var t = binding.getEncodedValueStruct(SerializationContext.empty(), deserializer, struct);

            return mapCodec.decode(typedOps, typedOps.getMap(t).getOrThrow()).getOrThrow();
        } catch (ClassCastException e) {
            throw new IllegalStateException("Unable to set the given encoded value into the passed Struct Deserializer as its not the correct type!", e);
        }
    }

    public interface CodecInteropBinding<T> {
        Class<? extends Serializer<T>> serializerClass();
        Class<? extends Deserializer<T>> deserializerClass();
        Class<? extends DynamicOps<T>> opsClass();

        //--

        Serializer<T> createSerializer();
        Deserializer<T> createDeserializer(T t);
        DynamicOps<T> getOps();

        //-

        T convertMapLike(MapLike<T> mapLike);
        RecordBuilder<T> addToBuilder(T t, RecordBuilder<T> builder);

        //--

        void setEncodedValue(Serializer<T> serializer, T t);
        T getEncodedValue(Deserializer<T> deserializer);

        //--

        void setEncodedValueStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, T t);
        T getEncodedValueStruct(SerializationContext ctx, Deserializer<?> serializer, Deserializer.Struct struct);
    }

    static {
        registerInteropBinding(new CodecInteropBinding<NbtElement>() {
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
            public Serializer<NbtElement> createSerializer() {
                return NbtSerializer.of();
            }

            @Override
            public Deserializer<NbtElement> createDeserializer(NbtElement tag) {
                return NbtDeserializer.of(tag);
            }

            @Override
            public DynamicOps<NbtElement> getOps() {
                return NbtOps.INSTANCE;
            }

            @Override
            public NbtElement convertMapLike(MapLike<NbtElement> mapLike) {
                return mapLike.entries().map(pair -> {
                    return pair.mapFirst(tag -> {
                        if(!(tag instanceof NbtString stringTag)) throw new IllegalStateException("Unable to parse key: " + tag);

                        return stringTag.asString();
                    });
                }).collect(Collector.of(NbtCompound::new, (compound, pair) -> compound.put(pair.getFirst(), pair.getSecond()), NbtCompound::copyFrom));
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
            public void setEncodedValue(Serializer<NbtElement> serializer, NbtElement tag) {
                ((SelfDescribedDeserializer<NbtElement>) createDeserializer(tag)).readAny(SerializationContext.empty(), serializer);
            }

            @Override
            public NbtElement getEncodedValue(Deserializer<NbtElement> deserializer) {
                var serializer = createSerializer();

                ((SelfDescribedDeserializer<NbtElement>) deserializer).readAny(SerializationContext.empty(), serializer);

                return serializer.result();
            }

            @Override
            public void setEncodedValueStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, NbtElement tag) {
                if(!(tag instanceof NbtCompound compoundTag)) {
                    throw new IllegalStateException("Unable to add to builder as the given Tag was not a CompoundTag: " + tag);
                }

                if (serializer instanceof SelfDescribedSerializer<?>) {
                    compoundTag.getKeys().forEach(key -> struct.field(key, ctx, NbtEndec.ELEMENT, compoundTag.get(key)));
                } else {
                    struct.field("element", ctx, NbtEndec.COMPOUND, compoundTag);
                }
            }

            @Override
            public NbtElement getEncodedValueStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
                return ((deserializer instanceof SelfDescribedDeserializer<?>)
                        ? NbtEndec.COMPOUND.decode(ctx, deserializer)
                        : struct.field("element", ctx, NbtEndec.ELEMENT));
            }
        });
    }
}