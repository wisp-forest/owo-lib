package io.wispforest.owo.serialization;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.owo.serialization.endecs.EitherEndec;
import io.wispforest.owo.serialization.impl.*;
import io.wispforest.owo.serialization.impl.edm.EdmDeserializer;
import io.wispforest.owo.serialization.impl.edm.EdmEndec;
import io.wispforest.owo.serialization.impl.edm.EdmOps;
import io.wispforest.owo.serialization.impl.edm.EdmSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;

/**
 * A combined <b>en</b>coder and <b>dec</b>oder for values of type {@code T}.
 * <p>
 * To convert between single instances of {@code T} and their serialized form,
 * use {@link #encodeFully(Supplier, Object)} and {@link #decodeFully(Function, Object)}
 */
public interface Endec<T> {

    /**
     * Write all data required to reconstruct {@code value} into {@code serializer}
     */
    void encode(Serializer<?> serializer, T value);

    /**
     * Decode the data specified by {@link #encode(Serializer, Object)} and reconstruct
     * the corresponding instance of {@code T}.
     * <p>
     * Endecs which intend to handle deserialization failure by decoding a different
     * structure on error, must wrap their initial reads in a call to {@link Deserializer#tryRead(Function)}
     * to ensure that deserializer state is restored for the subsequent attempt
     */
    T decode(Deserializer<?> deserializer);

    // ---

    /**
     * Create a new serializer with result type {@code E}, call {@link #encode(Serializer, Object)}
     * once for the provided {@code value} and return the serializer's {@linkplain Serializer#result() result}
     */
    default <E> E encodeFully(Supplier<Serializer<E>> serializerConstructor, T value) {
        var serializer = serializerConstructor.get();
        this.encode(serializer, value);

        return serializer.result();
    }

    /**
     * Create a new deserializer by calling {@code deserializerConstructor} with {@code value}
     * and return the result of {@link #decode(Deserializer)}
     */
    default <E> T decodeFully(Function<E, Deserializer<E>> deserializerConstructor, E value) {
        return this.decode(deserializerConstructor.apply(value));
    }

    // --- Serializer Primitives ---

    Endec<Void> VOID = Endec.of((serializer, unused) -> {}, deserializer -> null);

    Endec<Boolean> BOOLEAN = Endec.of(Serializer::writeBoolean, Deserializer::readBoolean);
    Endec<Byte> BYTE = Endec.of(Serializer::writeByte, Deserializer::readByte);
    Endec<Short> SHORT = Endec.of(Serializer::writeShort, Deserializer::readShort);
    Endec<Integer> INT = Endec.of(Serializer::writeInt, Deserializer::readInt);
    Endec<Integer> VAR_INT = Endec.of(Serializer::writeVarInt, Deserializer::readVarInt);
    Endec<Long> LONG = Endec.of(Serializer::writeLong, Deserializer::readLong);
    Endec<Long> VAR_LONG = Endec.of(Serializer::writeVarLong, Deserializer::readVarLong);
    Endec<Float> FLOAT = Endec.of(Serializer::writeFloat, Deserializer::readFloat);
    Endec<Double> DOUBLE = Endec.of(Serializer::writeDouble, Deserializer::readDouble);
    Endec<String> STRING = Endec.of(Serializer::writeString, Deserializer::readString);
    Endec<byte[]> BYTE_ARRAY = Endec.of(Serializer::writeBytes, Deserializer::readBytes);

    // --- Serializer compound types ---

    default Endec<List<T>> listOf() {
        return of((serializer, list) -> {
            try (var sequence = serializer.sequence(this, list.size())) {
                list.forEach(sequence::element);
            }
        }, deserializer -> {
            return Lists.newArrayList(deserializer.sequence(this));
        });
    }

    default Endec<Map<String, T>> mapOf() {
        return of((serializer, map) -> {
            try (var mapState = serializer.map(this, map.size())) {
                map.forEach(mapState::entry);
            }
        }, deserializer -> {
            var mapState = deserializer.map(this);

            var map = new HashMap<String, T>(mapState.estimatedSize());
            mapState.forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue()));

            return map;
        });
    }

    default Endec<Optional<T>> optionalOf() {
        return of(
                (serializer, value) -> serializer.writeOptional(this, value),
                deserializer -> deserializer.readOptional(this)
        );
    }

    // --- Constructors ---

    static <T> Endec<T> of(BiConsumer<Serializer<?>, T> encode, Function<Deserializer<?>, T> decode) {
        return new Endec<>() {
            @Override
            public void encode(Serializer<?> serializer, T value) {
                encode.accept(serializer, value);
            }

            @Override
            public T decode(Deserializer<?> deserializer) {
                return decode.apply(deserializer);
            }
        };
    }

    static <K, V> Endec<Map<K, V>> map(Endec<K> keyEndec, Endec<V> valueEndec) {
        return StructEndecBuilder.<Map.Entry<K, V>, K, V>of(
                StructField.of("k", keyEndec, Map.Entry::getKey),
                StructField.of("v", valueEndec, Map.Entry::getValue),
                Map::entry
        ).listOf().xmap(entries -> Map.ofEntries(entries.toArray(Map.Entry[]::new)), kvMap -> List.copyOf(kvMap.entrySet()));
    }

    static <T> Endec<T> ofCodec(Codec<T> codec) {
        return of(
                (serializer, value) -> EdmEndec.INSTANCE.encode(serializer, codec.encodeStart(EdmOps.INSTANCE, value).result().get()),
                deserializer -> codec.parse(EdmOps.INSTANCE, EdmEndec.INSTANCE.decode(deserializer)).result().get()
        );
    }

    // ---

    static <T, K> Endec<T> dispatchedStruct(Function<K, StructEndec<? extends T>> variantToEndec, Function<T, K> instanceToVariant, Endec<K> variantEndec) {
        return dispatchedStruct(variantToEndec, instanceToVariant, variantEndec, "type");
    }

    static <T, K> Endec<T> dispatchedStruct(Function<K, StructEndec<? extends T>> variantToEndec, Function<T, K> instanceToVariant, Endec<K> variantEndec, String variantKey) {
        return new StructEndec<>() {
            @Override
            public void encodeStruct(Serializer.Struct struct, T value) {
                var variant = instanceToVariant.apply(value);
                struct.field(variantKey, variantEndec, variant);

                //noinspection unchecked
                ((StructEndec<T>) variantToEndec.apply(variant)).encodeStruct(struct, value);
            }

            @Override
            public T decodeStruct(Deserializer.Struct struct) {
                var variant = struct.field(variantKey, variantEndec);
                return variantToEndec.apply(variant).decodeStruct(struct);
            }
        };
    }

    static <T, K> Endec<T> dispatched(Function<K, Endec<? extends T>> variantToEndec, Function<T, K> instanceToVariant, Endec<K> variantEndec) {
        return new StructEndec<>() {
            @Override
            public void encodeStruct(Serializer.Struct struct, T value) {
                var variant = instanceToVariant.apply(value);
                struct.field("variant", variantEndec, variant);

                //noinspection unchecked
                struct.field("instance", ((Endec<T>) variantToEndec.apply(variant)), value);
            }

            @Override
            public T decodeStruct(Deserializer.Struct struct) {
                var variant = struct.field("variant", variantEndec);
                return struct.field("instance", variantToEndec.apply(variant));
            }
        };
    }

    // ---

    static <F, S> Endec<Either<F, S>> either(Endec<F> first, Endec<S> second) {
        return new EitherEndec<>(first, second, false);
    }

    static <F, S> Endec<Either<F, S>> xor(Endec<F> first, Endec<S> second) {
        return new EitherEndec<>(first, second, true);
    }

    // ---

    static <T> AttributeEndecBuilder<T> ifAttr(SerializationAttribute attribute, Endec<T> endec) {
        return new AttributeEndecBuilder<>(endec, attribute);
    }

    // --- Endec composition ---

    default <R> Endec<R> xmap(Function<T, R> to, Function<R, T> from) {
        return of(
                (serializer, value) -> Endec.this.encode(serializer, from.apply(value)),
                deserializer -> to.apply(Endec.this.decode(deserializer))
        );
    }

    default Endec<T> validate(Consumer<T> validator) {
        return this.xmap(t -> {
            validator.accept(t);
            return t;
        }, t -> {
            validator.accept(t);
            return t;
        });
    }

    default Endec<T> catchErrors(BiFunction<Deserializer<?>, Exception, T> decodeOnError) {
        return of(this::encode, deserializer -> {
            try {
                return deserializer.tryRead(this::decode);
            } catch (Exception e) {
                return decodeOnError.apply(deserializer, e);
            }
        });
    }

    // --- Conversion ---

    default Codec<T> codec(SerializationAttribute... assumedAttributes) {
        return new Codec<>() {
            @Override
            public <D> DataResult<Pair<T, D>> decode(DynamicOps<D> ops, D input) {
                try {
                    return DataResult.success(new Pair<>(Endec.this.decode(new EdmDeserializer(ops.convertTo(EdmOps.INSTANCE, input), assumedAttributes)), input));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }

            @Override
            public <D> DataResult<D> encode(T input, DynamicOps<D> ops, D prefix) {
                try {
                    return DataResult.success(EdmOps.INSTANCE.convertTo(ops, Endec.this.encodeFully(() -> new EdmSerializer(assumedAttributes), input)));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }
        };
    }

    default KeyedField<T> keyed(String name) {
        return KeyedField.of(name, this);
    }

    default <S> StructField<S, T> field(String name, Function<S, T> getter) {
        return StructField.of(name, this, getter);
    }

    default Endec<@Nullable T> nullableOf() {
        return this.optionalOf().xmap(o -> o.orElse(null), Optional::ofNullable);
    }
}
