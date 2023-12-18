package io.wispforest.owo.serialization;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.owo.serialization.endec.*;
import io.wispforest.owo.serialization.format.edm.*;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
    Endec<byte[]> BYTES = Endec.of(Serializer::writeBytes, Deserializer::readBytes);

    // --- Serializer compound types ---

    /**
     * Create a new endec which serializes a list of elements
     * serialized using this endec
     */
    default Endec<List<T>> listOf() {
        return of((serializer, list) -> {
            try (var sequence = serializer.sequence(this, list.size())) {
                list.forEach(sequence::element);
            }
        }, deserializer -> {
            var sequenceState = deserializer.sequence(this);

            var list = new ArrayList<T>(sequenceState.estimatedSize());
            sequenceState.forEachRemaining(list::add);

            return list;
        });
    }

    /**
     * Create a new endec which serializes a map from string
     * keys to values serialized using this endec
     */
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

    /**
     * Create a new endec which serializes an optional value
     * serialized using this endec
     */
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

    /**
     * Create a new endec which serializes a map from keys serialized using
     * {@code keyEndec} to values serialized using {@code valueEndec}.
     * <p>
     * Due to the endec data model only natively supporting maps
     * with string keys, the resulting endec's serialized representation
     * is a list of key-value pairs
     */
    @SuppressWarnings("unchecked")
    static <K, V> Endec<Map<K, V>> map(Endec<K> keyEndec, Endec<V> valueEndec) {
        return StructEndecBuilder.of(
                keyEndec.fieldOf("k", Map.Entry::getKey),
                valueEndec.fieldOf("v", Map.Entry::getValue),
                Map::entry
        ).listOf().xmap(entries -> Map.ofEntries(entries.toArray(Map.Entry[]::new)), kvMap -> List.copyOf(kvMap.entrySet()));
    }

    /**
     * Create a new endec which serializes the enum constants of {@code enumClass}
     * <p>
     * In a human-readable format, the endec serializes to the {@linkplain Enum#name() constant's name},
     * and to its {@linkplain Enum#ordinal() ordinal} otherwise
     */
    static <E extends Enum<E>> Endec<E> forEnum(Class<E> enumClass) {
        return ifAttr(
                SerializationAttribute.HUMAN_READABLE,
                STRING.xmap(name -> Arrays.stream(enumClass.getEnumConstants()).filter(e -> e.name().equals(name)).findFirst().get(), Enum::name)
        ).orElse(
                VAR_INT.xmap(ordinal -> enumClass.getEnumConstants()[ordinal], Enum::ordinal)
        );
    }

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
    static <T> Endec<T> ofCodec(Codec<T> codec) {
        return of(
                (serializer, value) -> EdmEndec.INSTANCE.encode(serializer, Util.getResult(codec.encodeStart(EdmOps.INSTANCE, value), IllegalStateException::new)),
                deserializer -> Util.getResult(codec.parse(EdmOps.INSTANCE, EdmEndec.INSTANCE.decode(deserializer)), IllegalStateException::new)
        );
    }

    // ---

    /**
     * Shorthand for {@link #dispatchedStruct(Function, Function, Endec, String)}
     * which always uses {@code type} as the {@code variantKey}
     */
    static <T, K> Endec<T> dispatchedStruct(Function<K, StructEndec<? extends T>> variantToEndec, Function<T, K> instanceToVariant, Endec<K> variantEndec) {
        return dispatchedStruct(variantToEndec, instanceToVariant, variantEndec, "type");
    }

    /**
     * Create a new struct-dispatch endec which serializes variants of the struct {@code T}
     * <p>
     * To do this, it inserts an additional field given by {@code variantKey} into the beginning of the
     * struct and writes the variant identifier obtained from {@code instanceToVariant} into it
     * using {@code variantEndec}. When decoding, this variant identifier is read and the rest
     * of the struct decoded with the endec obtained from {@code variantToEndec}
     * <p>
     * For example, assume there is some interface like this
     * <pre>{@code
     * public interface Herbert {
     *      Identifier id();
     *      ... more functionality here
     * }
     * }</pre>
     *
     * which is implemented by {@code Harald} and {@code Albrecht}, whose endecs we have
     * stored in a map:
     * <pre>{@code
     * public final class Harald implements Herbert {
     *      public static final Endec<Harald> = StructEndecBuilder.of(...);
     *
     *      private final int haraldOMeter;
     *      ...
     * }
     *
     * public final class Albrecht implements Herbert {
     *     public static final Endec<Harald> = StructEndecBuilder.of(...);
     *
     *     private final List<String> dadJokes;
     *      ...
     * }
     *
     * public static final Map<Identifier, StructEndec<? extends Herbert>> HERBERT_REGISTRY = Map.of(
     *      new Identifier("herbert", "harald"), Harald.ENDEC,
     *      new Identifier("herbert", "albrecht"), Albrecht.ENDEC
     * );
     * }</pre>
     *
     * We could then create an endec capable of serializing either {@code Harald} or {@code Albrecht} as follows:
     * <pre>{@code
     * Endec.dispatchedStruct(HERBERT_REGISTRY::get, Herbert::id, BuiltInEndecs.IDENTIFIER, "type")
     * }</pre>
     *
     * If now encode an instance of {@code Albrecht} to JSON using this endec, we'll get the following result:
     * <pre>{@code
     * {
     *      "type": "herbert:albrecht",
     *      "dad_jokes": [
     *          "What does a sprinter eat before a race? Nothing, they fast!",
     *          "Why don't eggs tell jokes? They'd crack each other up."
     *      ]
     * }
     * }</pre>
     *
     * And similarly, the following data could be used for decoding an instance of {@code Harald}:
     * <pre>{@code
     * {
     *      "type": "herbert:harald",
     *      "harald_o_meter": 69
     * }
     * }</pre>
     */
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

    /**
     * Create a new dispatch endec which serializes variants of {@code T}
     * <p>
     * Such an endec is conceptually similar to a struct-dispatch one created through {@link #dispatchedStruct(Function, Function, Endec, String)}
     * (check the documentation on that function for a complete usage example), but because this family of endecs does not
     * require {@code T} to be a struct, the variant identifier field cannot be merged with the rest and is encoded separately
     */
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

    /**
     * Create an endec which serializes an instance of {@link Either}, using {@code first}
     * for the left and {@code second} for the right variant
     * <p>
     * In a self-describing format, the serialized representation is simply that of the endec of
     * whichever variant is represented. In the general for non-self-described formats, the
     * which variant is represented must also be stored
     */
    private static <F, S> Endec<Either<F, S>> either(Endec<F> first, Endec<S> second) {
        return new EitherEndec<>(first, second, false);
    }

    /**
     * Like {@link #either(Endec, Endec)}, but ensures when decoding from a self-described format
     * that only {@code first} or {@code second}, but not both, succeed
     */
    static <F, S> Endec<Either<F, S>> xor(Endec<F> first, Endec<S> second) {
        return new EitherEndec<>(first, second, true);
    }

    // ---

    static <T> AttributeEndecBuilder<T> ifAttr(SerializationAttribute attribute, Endec<T> endec) {
        return new AttributeEndecBuilder<>(endec, attribute);
    }

    // --- Endec composition ---

    /**
     * Create a new endec which converts between instances of {@code T} and {@code R}
     * using {@code to} and {@code from} before encoding / after decoding
     */
    default <R> Endec<R> xmap(Function<T, R> to, Function<R, T> from) {
        return of(
                (serializer, value) -> Endec.this.encode(serializer, from.apply(value)),
                deserializer -> to.apply(Endec.this.decode(deserializer))
        );
    }

    /**
     * Create a new endec which runs {@code validator} (giving it the chance to throw on
     * an invalid value) before encoding / after decoding
     */
    default Endec<T> validate(Consumer<T> validator) {
        return this.xmap(t -> {
            validator.accept(t);
            return t;
        }, t -> {
            validator.accept(t);
            return t;
        });
    }

    /**
     * Create a new endec which, if decoding using this endec's {@link #decode(Deserializer)} fails,
     * instead tries to decode using {@code decodeOnError}
     */
    default Endec<T> catchErrors(BiFunction<Deserializer<?>, Exception, T> decodeOnError) {
        return of(this::encode, deserializer -> {
            try {
                return deserializer.tryRead(this::decode);
            } catch (Exception e) {
                return decodeOnError.apply(deserializer, e);
            }
        });
    }

    /**
     * Create a new endec by wrapping {@link #optionalOf()} and mapping between
     * present optional &lt;-&gt; value and empty optional &lt;-&gt; null
     */
    default Endec<@Nullable T> nullableOf() {
        return this.optionalOf().xmap(o -> o.orElse(null), Optional::ofNullable);
    }

    // --- Conversion ---

    /**
     * Create a codec serializing the same data as this endec, assuming
     * that the serialized format posses the {@code assumedAttributes}
     * <p>
     * This method is implemented by converting between a given DynamicOps'
     * datatype and EDM (see {@link #ofCodec(Codec)}) and then encoding/decoding
     * from/to an EDM element using the {@link EdmSerializer} and {@link EdmDeserializer}
     * <p>
     * The serialized representation of a codec created through this method is generally
     * identical to that of a codec manually created to describe the same data
     */
    default Codec<T> codec(SerializationAttribute... assumedAttributes) {
        return new Codec<>() {
            @Override
            public <D> DataResult<Pair<T, D>> decode(DynamicOps<D> ops, D input) {
                try {
                    return DataResult.success(new Pair<>(Endec.this.decode(new LenientEdmDeserializer(ops.convertTo(EdmOps.INSTANCE, input), assumedAttributes)), input));
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

    /**
     * Create a new keyed endec which (de)serializes the entry
     * with key {@code key} into/from a {@link io.wispforest.owo.serialization.util.MapCarrier},
     * decoding to {@code defaultValue} if the map does not contain such an entry
     * <p>
     * If {@code T} is of a mutable type, you almost always want to use {@link #keyed(String, Supplier)} instead
     */
    default KeyedEndec<T> keyed(String key, T defaultValue) {
        return new KeyedEndec<>(key, this, defaultValue);
    }

    /**
     * Create a new keyed endec which (de)serializes the entry
     * with key {@code key} into/from a {@link io.wispforest.owo.serialization.util.MapCarrier},
     * decoding to the result of invoking {@code defaultValueFactory} if the map does not contain such an entry
     * <p>
     * If {@code T} is of an immutable type, you almost always want to use {@link #keyed(String, Object)} instead
     */
    default KeyedEndec<T> keyed(String key, Supplier<T> defaultValueFactory) {
        return new KeyedEndec<>(key, this, defaultValueFactory);
    }

    // ---

    default <S> StructField<S, T> fieldOf(String name, Function<S, T> getter) {
        return new StructField<>(name, this, getter);
    }

    default <S> StructField<S, T> optionalFieldOf(String name, Function<S, T> getter, @Nullable T defaultValue) {
        return new StructField<>(name, this.optionalOf().xmap(optional -> optional.orElse(defaultValue), Optional::ofNullable), getter, defaultValue);
    }

    default <S> StructField<S, T> optionalFieldOf(String name, Function<S, T> getter, Supplier<@Nullable T> defaultValue) {
        return new StructField<>(name, this.optionalOf().xmap(optional -> optional.orElseGet(defaultValue), Optional::ofNullable), getter, defaultValue);
    }
}
