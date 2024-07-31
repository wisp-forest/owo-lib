package io.wispforest.owo.serialization.endec;

import com.mojang.datafixers.util.Function3;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Function;

public final class MinecraftEndecs {

    private MinecraftEndecs() {}

    // --- MC Types ---

    public static final Endec<PacketByteBuf> PACKET_BYTE_BUF = Endec.BYTES
            .xmap(bytes -> {
                var buffer = PacketByteBufs.create();
                buffer.writeBytes(bytes);

                return buffer;
            }, buffer -> {
                var rinx = buffer.readerIndex();

                var bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);

                buffer.readerIndex(rinx);

                return bytes;
            });

    public static final Endec<Identifier> IDENTIFIER = Endec.STRING.xmap(Identifier::of, Identifier::toString);
    public static final Endec<ItemStack> ITEM_STACK = CodecUtils.toEndec(ItemStack.OPTIONAL_CODEC);
    public static final Endec<Text> TEXT = CodecUtils.toEndec(TextCodecs.CODEC, TextCodecs.PACKET_CODEC);

    public static final Endec<Vec3i> VEC3I = vectorEndec("Vec3i", Endec.INT, Vec3i::new, Vec3i::getX, Vec3i::getY, Vec3i::getZ);
    public static final Endec<Vec3d> VEC3D = vectorEndec("Vec3d", Endec.DOUBLE, Vec3d::new, Vec3d::getX, Vec3d::getY, Vec3d::getZ);
    public static final Endec<Vector3f> VECTOR3F = vectorEndec("Vector3f", Endec.FLOAT, Vector3f::new, Vector3f::x, Vector3f::y, Vector3f::z);

    public static final Endec<BlockPos> BLOCK_POS = Endec
            .ifAttr(
                    SerializationAttributes.HUMAN_READABLE,
                    vectorEndec("BlockPos", Endec.INT, BlockPos::new, BlockPos::getX, BlockPos::getY, BlockPos::getZ)
            ).orElse(
                    Endec.LONG.xmap(BlockPos::fromLong, BlockPos::asLong)
            );

    public static final Endec<ChunkPos> CHUNK_POS = Endec
            .ifAttr(
                    SerializationAttributes.HUMAN_READABLE,
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

    public static final Endec<BlockHitResult> BLOCK_HIT_RESULT = StructEndecBuilder.of(
            VEC3D.fieldOf("pos", BlockHitResult::getPos),
            Endec.forEnum(Direction.class).fieldOf("side", BlockHitResult::getSide),
            BLOCK_POS.fieldOf("block_pos", BlockHitResult::getBlockPos),
            Endec.BOOLEAN.fieldOf("inside_block", BlockHitResult::isInsideBlock),
            Endec.BOOLEAN.fieldOf("missed", $ -> $.getType() == HitResult.Type.MISS),
            (pos, side, blockPos, insideBlock, missed) -> !missed
                    ? new BlockHitResult(pos, side, blockPos, insideBlock)
                    : BlockHitResult.createMissed(pos, side, blockPos)
    );

    // --- Constructors for MC types ---

    public static ReflectiveEndecBuilder addDefaults(ReflectiveEndecBuilder builder) {
        builder.register(PACKET_BYTE_BUF, PacketByteBuf.class);

        builder.register(IDENTIFIER, Identifier.class)
                .register(ITEM_STACK, ItemStack.class)
                .register(TEXT, Text.class);

        builder.register(VEC3I, Vec3i.class)
                .register(VEC3D, Vec3d.class)
                .register(VECTOR3F, Vector3f.class);

        builder.register(BLOCK_POS, BlockPos.class)
                .register(CHUNK_POS, ChunkPos.class);

        builder.register(BLOCK_HIT_RESULT, BlockHitResult.class);

        return builder;
    }

    public static <T> Endec<T> ofRegistry(Registry<T> registry) {
        return IDENTIFIER.xmap(registry::get, registry::getId);
    }

    public static <T> Endec<TagKey<T>> unprefixedTagKey(RegistryKey<? extends Registry<T>> registry) {
        return IDENTIFIER.xmap(id -> TagKey.of(registry, id), TagKey::id);
    }

    public static <T> Endec<TagKey<T>> prefixedTagKey(RegistryKey<? extends Registry<T>> registry) {
        return Endec.STRING.xmap(
                s -> TagKey.of(registry, Identifier.of(s.substring(1))),
                tag -> "#" + tag.id()
        );
    }

    private static <C, V> Endec<V> vectorEndec(String name, Endec<C> componentEndec, Function3<C, C, C, V> constructor, Function<V, C> xGetter, Function<V, C> yGetter, Function<V, C> zGetter) {
        return componentEndec.listOf().validate(ints -> {
            if (ints.size() != 3) {
                throw new IllegalStateException(name + " array must have three elements");
            }
        }).xmap(
                components -> constructor.apply(components.get(0), components.get(1), components.get(2)),
                vector -> List.of(xGetter.apply(vector), yGetter.apply(vector), zGetter.apply(vector))
        );
    }
}
