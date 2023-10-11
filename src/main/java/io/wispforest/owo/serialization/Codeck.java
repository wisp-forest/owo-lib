package io.wispforest.owo.serialization;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
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
//            BYTE.list()
//            .then(list -> {
//                byte[] bytes = new byte[list.size()];
//                for (int i = 0; i < list.size(); i++) bytes[i] = list.get(i);
//                return bytes;
//            }, bytes -> {
//                List<Byte> list = new ArrayList<>();
//                for (byte Byte : bytes) list.add(Byte);
//                return list;
//            });

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
                describedSerializer.writeAny(new JsonDeserializer(value).readAny());

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
                var jsonSerializerzer = new JsonSerializer();

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
                describedSerializer.writeAny(new NbtDeserializer(value).readAny());

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
                var nbtSerializerzer = new NbtSerializer();

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

    Codeck<ItemStack> ITEM_STACK = COMPOUND
            .then(ItemStack::fromNbt, stack -> stack.writeNbt(new NbtCompound()));

    Codeck<UUID> UUID = StructCodeckBuilder.of(
            StructField.of("mostSig", Codeck.LONG, java.util.UUID::getMostSignificantBits),
            StructField.of("leastSig", Codeck.LONG, java.util.UUID::getLeastSignificantBits),
            UUID::new
    );

    Codeck<Date> DATE = Codeck.LONG.then(Date::new, Date::getTime);

    Codeck<PacketByteBuf> PACKET_BYTE_BUF = Codeck.BYTE_ARRAY
            .then(
                    bytes -> {
                        var byteBuf = PacketByteBufs.create();

                        byteBuf.writeBytes(bytes);

                        return byteBuf;
                    },
                    byteBuf -> {
                        var bytes = new byte[byteBuf.readerIndex()];

                        byteBuf.readBytes(bytes);

                        return bytes;
                    }
            );

    Codeck<BlockPos> BLOCK_POS = Codeck.LONG.then(BlockPos::fromLong, BlockPos::asLong);
    Codeck<ChunkPos> CHUNK_POS = Codeck.LONG.then(ChunkPos::new, ChunkPos::toLong);

    Codeck<BitSet> BITSET = Codeck.LONG_ARRAY.then(BitSet::valueOf, BitSet::toLongArray);

    Codeck<Text> TEXT = Codeck.STRING.then(Text.Serializer::fromJson, Text.Serializer::toJson);


    //--

    //Kinda mega cursed but...
    static <e, T> Codeck<T> of(BiConsumer<Serializer<e>, T> encode, Function<Deserializer<e>, T> decode) {
        return new Codeck<T>() {
            @Override
            public <E> void encode(Serializer<E> serializer, T value) {
                encode.accept((Serializer<e>) serializer, value);
            }

            @Override
            public <E> T decode(Deserializer<E> deserializer) {
                return decode.apply((Deserializer<e>) deserializer);
            }
        };
    }

    default ListCodeck<T> list(){
        return new ListCodeck<>(this);
    }

    default MapCodeck<String, T> map(){
        return MapCodeck.of(this);
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
