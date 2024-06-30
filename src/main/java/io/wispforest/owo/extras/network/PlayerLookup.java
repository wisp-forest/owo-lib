package io.wispforest.owo.extras.network;

import io.wispforest.owo.mixin.extras.EntityTrackerAccessor;
import io.wispforest.owo.mixin.extras.ServerChunkLoadingManagerAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class PlayerLookup {

    public static Collection<ServerPlayerEntity> all(MinecraftServer server) {
        Objects.requireNonNull(server, "The server cannot be null");

        // return an immutable collection to guard against accidental removals.
        if (server.getPlayerManager() != null) {
            return Collections.unmodifiableCollection(server.getPlayerManager().getPlayerList());
        }

        return Collections.emptyList();
    }

    public static Collection<ServerPlayerEntity> tracking(ServerWorld world, ChunkPos pos) {
        Objects.requireNonNull(world, "The world cannot be null");
        Objects.requireNonNull(pos, "The chunk pos cannot be null");

        return world.getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(pos, false);
    }

    public static Collection<ServerPlayerEntity> tracking(Entity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        ChunkManager manager = entity.getWorld().getChunkManager();

        if (manager instanceof ServerChunkManager) {
            ServerChunkLoadingManager chunkLoadingManager = ((ServerChunkManager) manager).chunkLoadingManager;
            EntityTrackerAccessor tracker = (EntityTrackerAccessor) ((ServerChunkLoadingManagerAccessor) chunkLoadingManager).getEntityTrackers().get(entity.getId());

            // return an immutable collection to guard against accidental removals.
            if (tracker != null) {
                return tracker.getListeners()
                        .stream().map(PlayerAssociatedNetworkHandler::getPlayer).collect(Collectors.toUnmodifiableSet());
            }

            return Collections.emptySet();
        }

        throw new IllegalArgumentException("Only supported on server worlds!");
    }

    public static Collection<ServerPlayerEntity> tracking(BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "BlockEntity cannot be null");

        //noinspection ConstantConditions - IJ intrinsics don't know hasWorld == true will result in no null
        if (!blockEntity.hasWorld() || blockEntity.getWorld().isClient()) {
            throw new IllegalArgumentException("Only supported on server worlds!");
        }

        return tracking((ServerWorld) blockEntity.getWorld(), blockEntity.getPos());
    }

    public static Collection<ServerPlayerEntity> tracking(ServerWorld world, BlockPos pos) {
        Objects.requireNonNull(pos, "BlockPos cannot be null");

        return tracking(world, new ChunkPos(pos));
    }

}
