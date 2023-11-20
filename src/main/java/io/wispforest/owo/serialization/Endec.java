package io.wispforest.owo.serialization;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.owo.serialization.impl.*;
import io.wispforest.owo.serialization.impl.edm.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;

public interface Endec<T> {

    void encode(Serializer<?> serializer, T value);

    T decode(Deserializer<?> deserializer);

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

    default ListEndec<T> listOf() {
        return new ListEndec<>(this);
    }

    default MapEndec<String, T> mapOf() {
        return MapEndec.of(this);
    }

    default Endec<Optional<T>> optionalOf() {
        return new Endec<>() {
            @Override
            public void encode(Serializer<?> serializer, Optional<T> value) {
                serializer.writeOptional(Endec.this, value);
            }

            @Override
            public Optional<T> decode(Deserializer<?> deserializer) {
                return deserializer.readOptional(Endec.this);
            }
        };
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
        return Endec.of(
                (serializer, value) -> EdmEndec.INSTANCE.encode(serializer, codec.encodeStart(EdmOps.INSTANCE, value).result().get()),
                deserializer -> codec.<EdmElement<?>>parse(EdmOps.INSTANCE, EdmEndec.INSTANCE.decode(deserializer)).result().get()
        );
    }

    // ---

    static <T, K> Endec<T> dispatchedStruct(Function<K, StructEndec<? extends T>> keyToEndec, Function<T, K> instanceToKey, Endec<K> keyEndec) {
        return new StructEndec<>() {
            @Override
            public void encodeStruct(Serializer.Struct struct, T value) {
                var key = instanceToKey.apply(value);
                struct.field("type", keyEndec, key);

                //noinspection unchecked
                ((StructEndec<T>) keyToEndec.apply(key)).encodeStruct(struct, value);
            }

            @Override
            public T decodeStruct(Deserializer.Struct struct) {
                var key = struct.field("type", keyEndec);
                return keyToEndec.apply(key).decodeStruct(struct);
            }
        };
    }

    static <T, K> Endec<T> dispatched(Function<K, Endec<? extends T>> keyToEndec, Function<T, K> instanceToKey, Endec<K> keyEndec) {
        return new StructEndec<>() {
            @Override
            public void encodeStruct(Serializer.Struct struct, T value) {
                var key = instanceToKey.apply(value);
                struct.field("type", keyEndec, key);

                //noinspection unchecked
                struct.field("instance", ((Endec<T>) keyToEndec.apply(key)), value);
            }

            @Override
            public T decodeStruct(Deserializer.Struct struct) {
                var key = struct.field("key", keyEndec);
                return struct.field("instance", keyToEndec.apply(key));
            }
        };
    }

    // ---

    static <T> AttributeEndecBuilder<T> ifAttr(SerializationAttribute attribute, Endec<T> endec) {
        return new AttributeEndecBuilder<>(endec, attribute);
    }

    //--

    default <R> Endec<R> xmap(Function<T, R> getter, Function<R, T> setter) {
        return new Endec<>() {
            @Override
            public void encode(Serializer<?> serializer, R value) {
                Endec.this.encode(serializer, setter.apply(value));
            }

            @Override
            public R decode(Deserializer<?> deserializer) {
                return getter.apply(Endec.this.decode(deserializer));
            }
        };
    }

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
                    return DataResult.success(EdmOps.INSTANCE.convertTo(ops, Endec.this.encode(() -> new EdmSerializer(assumedAttributes), input)));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }
        };
    }

    default KeyedField<T> keyed(String name) {
        return KeyedField.of(name, this);
    }

    default <R> StructField<R, T> field(String name, Function<R, T> getter) {
        return StructField.of(name, this, getter);
    }

    default Endec<@Nullable T> nullableOf() {
        return this.optionalOf().xmap(o -> o.orElse(null), Optional::ofNullable);
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

    default Endec<T> catchErrors(TriConsumer<Serializer<?>, T, Exception> encode, BiFunction<Deserializer<?>, Exception, T> decode) {
        return new Endec<>() {
            @Override
            public void encode(Serializer<?> serializer, T value) {
                try {
                    Endec.this.encode(serializer, value);
                } catch (Exception e) {
                    encode.accept(serializer, value, e);
                }
            }

            @Override
            public T decode(Deserializer<?> deserializer) {
                try {
                    return deserializer.tryRead(Endec.this::decode);
                } catch (Exception e) {
                    return decode.apply(deserializer, e);
                }
            }
        };
    }

    // ---

    default <E> E encode(Supplier<Serializer<E>> serializerCreator, T value) {
        Serializer<E> serializer = serializerCreator.get();

        encode(serializer, value);

        return serializer.result();
    }

    default <E> T decode(Function<E, Deserializer<E>> deserializerCreator, E value) {
        return decode(deserializerCreator.apply(value));
    }

}
