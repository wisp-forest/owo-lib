package io.wispforest.owo.serialization;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.wispforest.owo.serialization.impl.*;
import io.wispforest.owo.serialization.impl.json.JsonDeserializer;
import io.wispforest.owo.serialization.impl.json.JsonSerializer;
import io.wispforest.owo.serialization.impl.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.impl.nbt.NbtSerializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Codeck<T> {

    Codeck<Void> EMPTY = Codeck.of((serializer, unused) -> {}, deserializer -> null);

    Codeck<Boolean> BOOLEAN = Codeck.of(Serializer::writeBoolean, Deserializer::readBoolean);
    Codeck<Byte> BYTE = Codeck.of(Serializer::writeByte, Deserializer::readByte);
    Codeck<Short> SHORT = Codeck.of(Serializer::writeShort, Deserializer::readShort);
    Codeck<Integer> INT = Codeck.of(Serializer::writeInt, Deserializer::readInt);
    Codeck<Long> LONG = Codeck.of(Serializer::writeLong, Deserializer::readLong);
    Codeck<Float> FLOAT = Codeck.of(Serializer::writeFloat, Deserializer::readFloat);
    Codeck<Double> DOUBLE = Codeck.of(Serializer::writeDouble, Deserializer::readDouble);
    Codeck<String> STRING = Codeck.of(Serializer::writeString, Deserializer::readString);

    Codeck<byte[]> BYTE_ARRAY = Codeck.of(Serializer::writeBytes, Deserializer::readBytes);

    Codeck<int[]> INT_ARRAY = INT.list()
            .then((list) -> list.stream().mapToInt(v -> v).toArray(), (ints) -> Arrays.stream(ints).boxed().toList());

    Codeck<long[]> LONG_ARRAY = LONG.list()
            .then((list) -> list.stream().mapToLong(v -> v).toArray(), (longs) -> Arrays.stream(longs).boxed().toList());

    //--

    Codeck<JsonElement> JSON_ELEMENT = new Codeck<JsonElement>() {
        private static final Logger LOGGER = LogUtils.getLogger();

        @Override
        public <E> void encode(Serializer<E> serializer, JsonElement value) {
            if(serializer instanceof SelfDescribedSerializer<E> describedSerializer){
                describedSerializer.writeAny(JsonDeserializer.of(value).readAny());

                return;
            }


            try {
                Codeck.STRING.encode(serializer, value.toString());
            } catch (AssertionError e){
                LOGGER.error("Unable to serialize the given NbtElement into the given format!");
                throw new RuntimeException(e);
            }
        }

        @Override
        public <E> JsonElement decode(Deserializer<E> deserializer) {
            if(deserializer instanceof SelfDescribedDeserializer<E> selfDescribedDeserializer){
                var jsonSerializerzer = JsonSerializer.of();

                jsonSerializerzer.writeAny(selfDescribedDeserializer.readAny());

                return jsonSerializerzer.result();
            }

            try {
                return new JsonStreamParser(Codeck.STRING.decode(deserializer)).next();
            } catch (JsonParseException e){
                LOGGER.error("Unable to deserialize the given format into the desired JsonElement!");
                throw new RuntimeException(e);
            }
        }
    };

    Codeck<NbtElement> NBT_ELEMENT = new Codeck<NbtElement>() {
        private static final Logger LOGGER = LogUtils.getLogger();
        @Override
        public <E> void encode(Serializer<E> serializer, NbtElement value) {
            if(serializer instanceof SelfDescribedSerializer<E> describedSerializer){
                describedSerializer.writeAny(NbtDeserializer.of(value).readAny());

                return;
            }

            try {
                ByteArrayDataOutput stream = ByteStreams.newDataOutput();
                NbtIo.write(value, stream);

                Codeck.BYTE_ARRAY.encode(serializer, stream.toByteArray());
            } catch (IOException e){
                LOGGER.error("Unable to serialize the given NbtElement into the given format!");
                throw new RuntimeException(e);
            }
        }

        @Override
        public <E> NbtElement decode(Deserializer<E> deserializer) {
            if(deserializer instanceof SelfDescribedDeserializer<E> selfDescribedDeserializer){
                var nbtSerializerzer = NbtSerializer.of();

                nbtSerializerzer.writeAny(selfDescribedDeserializer.readAny());

                return nbtSerializerzer.result();
            }

            byte[] array = Codeck.BYTE_ARRAY.decode(deserializer);

            try {
                ByteArrayDataInput stream = ByteStreams.newDataInput(array);

                return NbtIo.read(stream, NbtTagSizeTracker.ofUnlimitedBytes());
            } catch (IOException e){
                LOGGER.error("Unable to deserialize the given format into the desired NbtElement!");
                throw new RuntimeException(e);
            }
        }
    };

    Codeck<NbtCompound> COMPOUND = Codeck.NBT_ELEMENT.then(element -> ((NbtCompound) element), compound -> compound);

    //--

    Codeck<Identifier> IDENTIFIER = Codeck.STRING.then(Identifier::new, Identifier::toString);

    Codeck<ItemStack> ITEM_STACK = COMPOUND.then(ItemStack::fromNbt, stack -> stack.writeNbt(new NbtCompound()));

    Codeck<UUID> UUID = Codeck.ifAttr(Codeck.STRING.then(java.util.UUID::fromString, java.util.UUID::toString), SerializationAttribute.HUMAN_READABLE)
            .orElse(Codeck.INT_ARRAY.then(Uuids::toUuid, Uuids::toIntArray));

    Codeck<Date> DATE = Codeck.ifAttr(Codeck.STRING.then(s -> Date.from(Instant.parse(s)), date -> date.toInstant().toString()), SerializationAttribute.HUMAN_READABLE)
            .orElse(Codeck.LONG.then(Date::new, Date::getTime));

    Codeck<PacketByteBuf> PACKET_BYTE_BUF = Codeck.BYTE_ARRAY
            .then(bytes -> {
                var byteBuf = PacketByteBufs.create();

                byteBuf.writeBytes(bytes);

                return byteBuf;
            }, byteBuf -> {
                var bytes = new byte[byteBuf.readableBytes()];

                byteBuf.readBytes(bytes);

                return bytes;
            });

    Codeck<BlockPos> BLOCK_POS = Codeck
            .ifAttr(
                    StructCodeckBuilder.of(
                            StructField.of("x", StructCodeck.INT, BlockPos::getX),
                            StructField.of("y", StructCodeck.INT, BlockPos::getX),
                            StructField.of("z", StructCodeck.INT, BlockPos::getX),
                            BlockPos::new
                    ),
                    SerializationAttribute.HUMAN_READABLE
            )
            .orElseIf(
                    Codeck.INT.list().then(
                            ints -> new BlockPos(ints.get(0), ints.get(1), ints.get(2)),
                            blockPos -> List.of(blockPos.getX(), blockPos.getY(), blockPos.getZ())
                    ),
                    SerializationAttribute.COMPRESSED
            )
            .orElse(Codeck.LONG.then(BlockPos::fromLong, BlockPos::asLong));

    Codeck<ChunkPos> CHUNK_POS = Codeck
            .ifAttr(
                    StructCodeckBuilder.of(
                            StructField.of("x", StructCodeck.INT, (ChunkPos pos) -> pos.x),
                            StructField.of("z", StructCodeck.INT, (ChunkPos pos) -> pos.z),
                            ChunkPos::new
                    ),
                    SerializationAttribute.HUMAN_READABLE)
            .orElse(Codeck.LONG.then(ChunkPos::new, ChunkPos::toLong));

    Codeck<BitSet> BITSET = Codeck.LONG_ARRAY.then(BitSet::valueOf, BitSet::toLongArray);

    Codeck<Text> TEXT = Codeck.JSON_ELEMENT.then(Text.Serializer::fromJson, Text.Serializer::toJsonTree);
            //Codeck.STRING.then(Text.Serializer::fromJson, Text.Serializer::toJson);

    Codeck<Integer> VAR_INT = Codeck.of(Serializer::writeVarInt, Deserializer::readVarInt);

    Codeck<Long> VAR_LONG = Codeck.of(Serializer::writeVarLong, Deserializer::readVarLong);

    //--

    //Kinda mega cursed but...
    static <T> Codeck<T> of(BiConsumer<Serializer, T> encode, Function<Deserializer, T> decode) {
        return new Codeck<>() {
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

    static <T> Codeck<T> ofRegistry(Registry<T> registry) {
        return Codeck.IDENTIFIER.then(registry::get, registry::getId);
    }

    static <T, K> Codeck<T> dispatchedOf(Function<K, Codeck<? extends T>> keyToCodeck, Function<T, K> keyGetter, Codeck<K> keyCodeck) {
        return new StructCodeck<T>() {
            @Override
            public void encode(StructSerializer struct, T value) {
                var key = keyGetter.apply(value);

                struct.field("key", keyCodeck, key)
                        .field("value", (Codeck<T>) keyToCodeck.apply(key), value);
            }

            @Override
            public T decode(StructDeserializer struct) {
                return struct.field("key", keyToCodeck.apply(struct.field("value", keyCodeck)));
            }
        };
    }

    static <K, V> Codeck<Map<K, V>> mapOf(Codeck<K> keyCodeck, Codeck<V> valueCodeck){
        return mapOf(keyCodeck, valueCodeck, HashMap::new);
    }

    static <K, V> Codeck<Map<K, V>> mapOf(Codeck<K> keyCodeck, Codeck<V> valueCodeck, Supplier<Map<K, V>> supplier){
        Codeck<Map.Entry<K, V>> mapEntryKodeck = StructCodeckBuilder.of(
                StructField.of("k", keyCodeck, Map.Entry::getKey),
                StructField.of("V", valueCodeck, Map.Entry::getValue),
                Map::entry
        );

        return mapEntryKodeck.list().then(entries -> {
            Map<K, V> map = supplier.get();

            for (Map.Entry<K, V> entry : entries) map.put(entry.getKey(), entry.getValue());

            return map;
        }, kvMap -> List.copyOf(kvMap.entrySet()));
    }

    static <T> AttributeCodeckBuilder<T> ifAttr(Codeck<T> codeck, SerializationAttribute attribute){
        return new AttributeCodeckBuilder<>(codeck, attribute);
    }

    //--

    default ListCodeck<T> list(){
        return new ListCodeck<>(this);
    }

    default MapCodeck<String, T> map(){
        return MapCodeck.of(this);
    }

    default <R> Codeck<R> then(Function<T, R> getter, Function<R, T> setter) {
        return new Codeck<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, R value) {
                Codeck.this.encode(serializer, setter.apply(value));
            }

            @Override
            public <E> R decode(Deserializer<E> deserializer) {
                return getter.apply(Codeck.this.decode(deserializer));
            }
        };
    }

    default Codeck<Optional<T>> ofOptional(){
        return new Codeck<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, Optional<T> value) {
                serializer.writeOptional(Codeck.this, value);
            }

            @Override
            public <E> Optional<T> decode(Deserializer<E> deserializer) {
                return deserializer.readOptional(Codeck.this);
            }
        };
    }

    default Codeck<@Nullable T> ofNullable(){
        return ofOptional().then(o -> o.orElse(null), Optional::ofNullable);
    }

    default Codeck<T> onError(TriConsumer<Serializer, T, Exception> encode, BiFunction<Deserializer, Exception, T> decode){
        return new Codeck<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, T value) {
                try {
                    Codeck.this.encode(serializer, value);
                } catch (Exception e){
                    encode.accept(serializer, value, e);
                }
            }

            @Override
            public <E> T decode(Deserializer<E> deserializer) {
                try {
                    return Codeck.this.decode(deserializer);
                } catch (Exception e) {
                    return decode.apply(deserializer, e);
                }
            }
        };
    }

    //--

    <E> void encode(Serializer<E> serializer, T value);

    <E> T decode(Deserializer<E> deserializer);

    default <E> E encode(Supplier<Serializer<E>> serializerCreator, T value){
        Serializer<E> serializer = serializerCreator.get();

        encode(serializer, value);

        return serializer.result();
    }

    default <E> T decode(Function<E, Deserializer<E>> deserializerCreator, E value){
        return decode(deserializerCreator.apply(value));
    }

}
