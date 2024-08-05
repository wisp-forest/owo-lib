package io.wispforest.owo.serialization.endec;

import com.mojang.datafixers.util.Function3;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.math.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Function;

public final class MinecraftEndecs {

    private MinecraftEndecs() {}

    // --- MC Types ---

    public static final Endec<FriendlyByteBuf> PACKET_BYTE_BUF = Endec.BYTES
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

    public static final Endec<Identifier> IDENTIFIER = Endec.STRING.xmap(Identifier::parse, Identifier::toString);
    public static final Endec<ItemStack> ITEM_STACK = CodecUtils.toEndecWithRegistries(ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC);
    public static final Endec<Text> TEXT = CodecUtils.toEndec(ComponentSerialization.CODEC, ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC);

    public static final Endec<Vec3i> VEC3I = vectorEndec("Vec3i", Endec.INT, Vec3i::new, Vec3i::getX, Vec3i::getY, Vec3i::getZ);
    public static final Endec<Vec3> VEC3D = vectorEndec("Vec3d", Endec.DOUBLE, Vec3::new, Vec3::x, Vec3::y, Vec3::z);
    public static final Endec<Vector3f> VECTOR3F = vectorEndec("Vector3f", Endec.FLOAT, Vector3f::new, Vector3f::x, Vector3f::y, Vector3f::z);

    public static final Endec<BlockPos> BLOCK_POS = Endec
            .ifAttr(
                    SerializationAttributes.HUMAN_READABLE,
                    vectorEndec("BlockPos", Endec.INT, BlockPos::new, BlockPos::getX, BlockPos::getY, BlockPos::getZ)
            ).orElse(
                    Endec.LONG.xmap(BlockPos::of, BlockPos::asLong)
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
            VEC3D.fieldOf("pos", BlockHitResult::getLocation),
            Endec.forEnum(Direction.class).fieldOf("side", BlockHitResult::getDirection),
            BLOCK_POS.fieldOf("block_pos", BlockHitResult::getBlockPos),
            Endec.BOOLEAN.fieldOf("inside_block", BlockHitResult::isInside),
            Endec.BOOLEAN.fieldOf("missed", $ -> $.getType() == HitResult.Type.MISS),
            (pos, side, blockPos, insideBlock, missed) -> !missed
                    ? new BlockHitResult(pos, side, blockPos, insideBlock)
                    : BlockHitResult.miss(pos, side, blockPos)
    );

    // --- Constructors for MC types ---

    public static ReflectiveEndecBuilder addDefaults(ReflectiveEndecBuilder builder) {
        builder.register(PACKET_BYTE_BUF, FriendlyByteBuf.class);

        builder.register(IDENTIFIER, Identifier.class)
                .register(ITEM_STACK, ItemStack.class)
                .register(TEXT, Text.class);

        builder.register(VEC3I, Vec3i.class)
                .register(VEC3D, Vec3.class)
                .register(VECTOR3F, Vector3f.class);

        builder.register(BLOCK_POS, BlockPos.class)
                .register(CHUNK_POS, ChunkPos.class);

        builder.register(BLOCK_HIT_RESULT, BlockHitResult.class);

        return builder;
    }

    public static <T> Endec<T> ofRegistry(Registry<T> registry) {
        return IDENTIFIER.xmap(registry::get, registry::getId);
    }

    public static <T> Endec<TagKey<T>> unprefixedTagKey(ResourceKey<? extends Registry<T>> registry) {
        return IDENTIFIER.xmap(id -> TagKey.of(registry, id), TagKey::id);
    }

    public static <T> Endec<TagKey<T>> prefixedTagKey(ResourceKey<? extends Registry<T>> registry) {
        return Endec.STRING.xmap(
                s -> TagKey.of(registry, Identifier.parse(s.substring(1))),
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
