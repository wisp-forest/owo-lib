package io.wispforest.owo.serialization;

import io.wispforest.owo.serialization.impl.SerializationAttribute;
import io.wispforest.owo.serialization.impl.json.JsonEndec;
import io.wispforest.owo.serialization.impl.nbt.NbtEndec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

public final class BuiltInEndecs {

    private BuiltInEndecs() {}

    // --- Java Types ---

    public static final Endec<int[]> INT_ARRAY = Endec.INT.listOf().xmap((list) -> list.stream().mapToInt(v -> v).toArray(), (ints) -> Arrays.stream(ints).boxed().toList());
    public static final Endec<long[]> LONG_ARRAY = Endec.LONG.listOf().xmap((list) -> list.stream().mapToLong(v -> v).toArray(), (longs) -> Arrays.stream(longs).boxed().toList());

    public static final Endec<BitSet> BITSET = LONG_ARRAY.xmap(BitSet::valueOf, BitSet::toLongArray);

    public static final Endec<java.util.UUID> UUID = Endec.ifAttr(SerializationAttribute.HUMAN_READABLE, Endec.STRING.xmap(java.util.UUID::fromString, java.util.UUID::toString))
            .orElse(INT_ARRAY.xmap(Uuids::toUuid, Uuids::toIntArray));

    public static final Endec<Date> DATE = Endec.ifAttr(SerializationAttribute.HUMAN_READABLE, Endec.STRING.xmap(s -> Date.from(Instant.parse(s)), date -> date.toInstant().toString()))
            .orElse(Endec.LONG.xmap(Date::new, Date::getTime));

    // --- MC Types ---

    public static final Endec<Identifier> IDENTIFIER = Endec.STRING.xmap(Identifier::new, Identifier::toString);
    public static final Endec<ItemStack> ITEM_STACK = NbtEndec.COMPOUND.xmap(ItemStack::fromNbt, stack -> stack.writeNbt(new NbtCompound()));
    public static final Endec<Text> TEXT = JsonEndec.INSTANCE.xmap(Text.Serializer::fromJson, Text.Serializer::toJsonTree);

    public static final Endec<BlockPos> BLOCK_POS = Endec
            .ifAttr(
                    SerializationAttribute.HUMAN_READABLE,
                    Endec.INT.listOf().validate(ints -> {
                        if (ints.size() == 3) {
                            throw new IllegalStateException("BlockPos array must have three elements");
                        }
                    }).xmap(
                            ints -> new BlockPos(ints.get(0), ints.get(1), ints.get(2)),
                            blockPos -> List.of(blockPos.getX(), blockPos.getY(), blockPos.getZ())
                    )
            )
            .orElse(Endec.LONG.xmap(BlockPos::fromLong, BlockPos::asLong));

    public static final Endec<ChunkPos> CHUNK_POS = Endec
            .ifAttr(
                    SerializationAttribute.HUMAN_READABLE,
                    Endec.INT.listOf().validate(ints -> {
                        if (ints.size() != 2) {
                            throw new IllegalStateException("ChunkPos array must have two elements");
                        }
                    }).xmap(
                            ints -> new ChunkPos(ints.get(0), ints.get(1)),
                            chunkPos -> List.of(chunkPos.x, chunkPos.z)
                    )
            )
            .orElse(Endec.LONG.xmap(ChunkPos::new, ChunkPos::toLong));

    public static final Endec<PacketByteBuf> PACKET_BYTE_BUF = Endec.BYTE_ARRAY
            .xmap(bytes -> {
                var buffer = PacketByteBufs.create();
                buffer.writeBytes(bytes);

                return buffer;
            }, buffer -> {
                var bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);

                return bytes;
            });

    // --- Constructors for MC types ---

    static <T> Endec<T> ofRegistry(Registry<T> registry) {
        return IDENTIFIER.xmap(registry::get, registry::getId);
    }

    static <T> Endec<TagKey<T>> unprefixedTagKey(RegistryKey<? extends Registry<T>> registry) {
        return IDENTIFIER.xmap(id -> TagKey.of(registry, id), TagKey::id);
    }

    static <T> Endec<TagKey<T>> prefixedTagKey(RegistryKey<? extends Registry<T>> registry) {
        return Endec.STRING.xmap(
                s -> TagKey.of(registry, new Identifier(s.substring(1))),
                tag -> "#" + tag.id()
        );
    }

}
