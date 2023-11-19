package io.wispforest.owo.serialization;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.owo.serialization.endecs.EdmEndec;
import io.wispforest.owo.serialization.impl.*;
import io.wispforest.owo.serialization.impl.edm.EdmDeserializer;
import io.wispforest.owo.serialization.impl.edm.EdmElement;
import io.wispforest.owo.serialization.impl.edm.EdmOps;
import io.wispforest.owo.serialization.impl.edm.EdmSerializer;
import io.wispforest.owo.serialization.impl.json.JsonEndec;
import io.wispforest.owo.serialization.impl.nbt.NbtEndec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Endec<T> {

    Endec<Void> EMPTY = Endec.of((serializer, unused) -> {}, deserializer -> null);

    Endec<Boolean> BOOLEAN = Endec.of(Serializer::writeBoolean, Deserializer::readBoolean);
    Endec<Byte> BYTE = Endec.of(Serializer::writeByte, Deserializer::readByte);
    Endec<Short> SHORT = Endec.of(Serializer::writeShort, Deserializer::readShort);
    Endec<Integer> INT = Endec.of(Serializer::writeInt, Deserializer::readInt);
    Endec<Long> LONG = Endec.of(Serializer::writeLong, Deserializer::readLong);
    Endec<Float> FLOAT = Endec.of(Serializer::writeFloat, Deserializer::readFloat);
    Endec<Double> DOUBLE = Endec.of(Serializer::writeDouble, Deserializer::readDouble);
    Endec<String> STRING = Endec.of(Serializer::writeString, Deserializer::readString);

    Endec<byte[]> BYTE_ARRAY = Endec.of(Serializer::writeBytes, Deserializer::readBytes);

    Endec<int[]> INT_ARRAY = INT.list().xmap((list) -> list.stream().mapToInt(v -> v).toArray(), (ints) -> Arrays.stream(ints).boxed().toList());

    Endec<long[]> LONG_ARRAY = LONG.list().xmap((list) -> list.stream().mapToLong(v -> v).toArray(), (longs) -> Arrays.stream(longs).boxed().toList());

    //--

    Endec<JsonElement> JSON_ELEMENT = JsonEndec.INSTANCE;
    Endec<NbtElement> NBT_ELEMENT = NbtEndec.INSTANCE;

    Endec<NbtCompound> COMPOUND = Endec.NBT_ELEMENT.xmap(element -> ((NbtCompound) element), compound -> compound);

    //--

    Endec<Identifier> IDENTIFIER = Endec.STRING.xmap(Identifier::new, Identifier::toString);

    Endec<ItemStack> ITEM_STACK = COMPOUND.xmap(ItemStack::fromNbt, stack -> stack.writeNbt(new NbtCompound()));

    Endec<UUID> UUID = Endec.ifAttr(Endec.STRING.xmap(java.util.UUID::fromString, java.util.UUID::toString), SerializationAttribute.HUMAN_READABLE)
            .orElse(Endec.INT_ARRAY.xmap(Uuids::toUuid, Uuids::toIntArray));

    Endec<Date> DATE = Endec.ifAttr(Endec.STRING.xmap(s -> Date.from(Instant.parse(s)), date -> date.toInstant().toString()), SerializationAttribute.HUMAN_READABLE)
            .orElse(Endec.LONG.xmap(Date::new, Date::getTime));

    Endec<PacketByteBuf> PACKET_BYTE_BUF = Endec.BYTE_ARRAY
            .xmap(bytes -> {
                var byteBuf = PacketByteBufs.create();

                byteBuf.writeBytes(bytes);

                return byteBuf;
            }, byteBuf -> {
                var bytes = new byte[byteBuf.readableBytes()];

                byteBuf.readBytes(bytes);

                return bytes;
            });

    Endec<BlockPos> BLOCK_POS = Endec
            .ifAttr(
                    Endec.INT.list().xmap(
                            ints -> new BlockPos(ints.get(0), ints.get(1), ints.get(2)),
                            blockPos -> List.of(blockPos.getX(), blockPos.getY(), blockPos.getZ())
                    ),
                    SerializationAttribute.HUMAN_READABLE
            )
            .orElse(Endec.LONG.xmap(BlockPos::fromLong, BlockPos::asLong));

    Endec<ChunkPos> CHUNK_POS = Endec
            .ifAttr(
                    StructEndecBuilder.of(
                            StructField.of("x", StructEndec.INT, (ChunkPos pos) -> pos.x),
                            StructField.of("z", StructEndec.INT, (ChunkPos pos) -> pos.z),
                            ChunkPos::new
                    ),
                    SerializationAttribute.HUMAN_READABLE
            )
            .orElse(Endec.LONG.xmap(ChunkPos::new, ChunkPos::toLong));

    Endec<BitSet> BITSET = Endec.LONG_ARRAY.xmap(BitSet::valueOf, BitSet::toLongArray);

    Endec<Text> TEXT = Endec.JSON_ELEMENT.xmap(Text.Serializer::fromJson, Text.Serializer::toJsonTree);
    //endec.STRING.then(Text.Serializer::fromJson, Text.Serializer::toJson);

    Endec<Integer> VAR_INT = Endec.of(Serializer::writeVarInt, Deserializer::readVarInt);

    Endec<Long> VAR_LONG = Endec.of(Serializer::writeVarLong, Deserializer::readVarLong);

    //--

    //Kinda mega cursed but...
    @SuppressWarnings("rawtypes")
    static <T> Endec<T> of(BiConsumer<Serializer, T> encode, Function<Deserializer, T> decode) {
        return new Endec<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, T value) {
                encode.accept(serializer, value);
            }

            @Override
            public <E> T decode(Deserializer<E> deserializer) {
                return decode.apply(deserializer);
            }
        };
    }

    static <T> Endec<T> ofRegistry(Registry<T> registry) {
        return Endec.IDENTIFIER.xmap(registry::get, registry::getId);
    }

    static <T> Endec<TagKey<T>> unprefixedTagKey(RegistryKey<? extends Registry<T>> registry) {
        return IDENTIFIER.xmap(id -> TagKey.of(registry, id), TagKey::id);
    }

    static <T> Endec<T> ofCodec(Codec<T> codec) {
        return Endec.of(
                (serializer, value) -> EdmEndec.INSTANCE.encode(serializer, codec.encodeStart(EdmOps.INSTANCE, value).result().get()),
                deserializer -> codec.<EdmElement<?>>parse(EdmOps.INSTANCE, EdmEndec.INSTANCE.decode(deserializer)).result().get()
        );
    }

    static <T> Endec<TagKey<T>> tagKey(RegistryKey<? extends Registry<T>> registry) {
        return Endec.STRING
                .validate(s -> {
                    if (!s.startsWith("#")) throw new IllegalStateException("Not a tag id");

                    var id = s.substring(1);

                    try {
                        if (!Identifier.isValid(id)) {
                            throw new IllegalStateException("Not a valid resource location: " + id);
                        }
                    } catch (InvalidIdentifierException var2) {
                        throw new IllegalStateException("Not a valid resource location: " + id + " " + var2.getMessage());
                    }

                    return s;
                })
                .xmap(
                        s -> TagKey.of(registry, new Identifier(s.substring(1))),
                        tag -> "#" + tag.id()
                );
    }

    static <T, K> Endec<T> dispatchedOf(Function<K, Endec<? extends T>> keyToEndec, Function<T, K> keyGetter, Endec<K> keyEndec) {
        return new StructEndec<T>() {
            @Override
            public void encode(Serializer.Struct struct, T value) {
                var key = keyGetter.apply(value);

                struct.field("key", keyEndec, key)
                        .field("value", (Endec<T>) keyToEndec.apply(key), value);
            }

            @Override
            public T decode(Deserializer.Struct struct) {
                return struct.field("key", keyToEndec.apply(struct.field("value", keyEndec)));
            }
        };
    }

    static <K, V> Endec<Map<K, V>> mapOf(Endec<K> keyEndec, Endec<V> valueEndec) {
        return mapOf(keyEndec, valueEndec, HashMap::new);
    }

    static <K, V> Endec<Map<K, V>> mapOf(Endec<K> keyEndec, Endec<V> valueEndec, Supplier<Map<K, V>> supplier) {
        Endec<Map.Entry<K, V>> mapEntryEndec = StructEndecBuilder.of(
                StructField.of("k", keyEndec, Map.Entry::getKey),
                StructField.of("V", valueEndec, Map.Entry::getValue),
                Map::entry
        );

        return mapEntryEndec.list().xmap(entries -> {
            Map<K, V> map = supplier.get();

            for (Map.Entry<K, V> entry : entries) map.put(entry.getKey(), entry.getValue());

            return map;
        }, kvMap -> List.copyOf(kvMap.entrySet()));
    }

    static <T> AttributeEndecBuilder<T> ifAttr(Endec<T> endec, SerializationAttribute attribute) {
        return new AttributeEndecBuilder<>(endec, attribute);
    }

    //--

    default ListEndec<T> list() {
        return new ListEndec<>(this);
    }

    default MapEndec<String, T> map() {
        return MapEndec.of(this);
    }

    default <R> Endec<R> xmap(Function<T, R> getter, Function<R, T> setter) {
        return new Endec<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, R value) {
                Endec.this.encode(serializer, setter.apply(value));
            }

            @Override
            public <E> R decode(Deserializer<E> deserializer) {
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

    default Endec<Optional<T>> ofOptional() {
        return new Endec<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, Optional<T> value) {
                serializer.writeOptional(Endec.this, value);
            }

            @Override
            public <E> Optional<T> decode(Deserializer<E> deserializer) {
                return deserializer.readOptional(Endec.this);
            }
        };
    }

    default Endec<@Nullable T> ofNullable() {
        return ofOptional().xmap(o -> o.orElse(null), Optional::ofNullable);
    }

    default Endec<T> validate(Function<T, T> validator) {
        return new Endec<T>() {
            @Override
            public <E> void encode(Serializer<E> serializer, T value) {
                Endec.this.encode(serializer, value);
            }

            @Override
            public <E> T decode(Deserializer<E> deserializer) {
                return validator.apply(Endec.this.decode(deserializer));
            }
        };
    }

    default Endec<T> onError(TriConsumer<Serializer, T, Exception> encode, BiFunction<Deserializer, Exception, T> decode) {
        return new Endec<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, T value) {
                try {
                    Endec.this.encode(serializer, value);
                } catch (Exception e) {
                    encode.accept(serializer, value, e);
                }
            }

            @Override
            public <E> T decode(Deserializer<E> deserializer) {
                try {
                    return deserializer.tryRead(Endec.this::decode);
                } catch (Exception e) {
                    return decode.apply(deserializer, e);
                }
            }
        };
    }

    //--

    <E> void encode(Serializer<E> serializer, T value);

    <E> T decode(Deserializer<E> deserializer);

    default <E> E encode(Supplier<Serializer<E>> serializerCreator, T value) {
        Serializer<E> serializer = serializerCreator.get();

        encode(serializer, value);

        return serializer.result();
    }

    default <E> T decode(Function<E, Deserializer<E>> deserializerCreator, E value) {
        return decode(deserializerCreator.apply(value));
    }

}
