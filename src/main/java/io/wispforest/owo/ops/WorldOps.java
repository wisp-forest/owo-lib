package io.wispforest.owo.ops;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockEntityProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of common operations done on {@link Level}
 */
public final class WorldOps {

    private WorldOps() {}

    /**
     * Break the specified block with the given item
     *
     * @param world     The world the block is in
     * @param pos       The position of the block to break
     * @param breakItem The item to break the block with
     */
    public static void breakBlockWithItem(Level world, BlockPos pos, ItemStack breakItem) {
        breakBlockWithItem(world, pos, breakItem, null);
    }

    /**
     * Break the specified block with the given item
     *
     * @param world          The world the block is in
     * @param pos            The position of the block to break
     * @param breakItem      The item to break the block with
     * @param breakingEntity The entity which is breaking the block
     */
    public static void breakBlockWithItem(Level world, BlockPos pos, ItemStack breakItem, @Nullable Entity breakingEntity) {
        BlockEntity breakEntity = world.getBlockState(pos).getBlock() instanceof BlockEntityProvider ? world.getBlockEntity(pos) : null;
        Block.dropResources(world.getBlockState(pos), world, pos, breakEntity, breakingEntity, breakItem);
        world.destroyBlock(pos, false, breakingEntity);
    }

    /**
     * Plays the provided sound at the provided location. This works on both client
     * and server. Volume and pitch default to 1
     *
     * @param world    The world to play the sound in
     * @param pos      Where to play the sound
     * @param sound    The sound to play
     * @param category The category for the sound
     */
    public static void playSound(Level world, Vec3 pos, SoundEvent sound, SoundSource category) {
        playSound(world, BlockPos.of(pos), sound, category, 1, 1);
    }

    public static void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource category) {
        playSound(world, pos, sound, category, 1, 1);
    }

    /**
     * Plays the provided sound at the provided location. This works on both client
     * and server
     *
     * @param world    The world to play the sound in
     * @param pos      Where to play the sound
     * @param sound    The sound to play
     * @param category The category for the sound
     * @param volume   The volume to play the sound at
     * @param pitch    The pitch, or speed, to play the sound at
     */
    public static void playSound(Level world, Vec3 pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, BlockPos.of(pos), sound, category, volume, pitch);
    }

    public static void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(null, pos, sound, category, volume, pitch);
    }

    /**
     * Causes a block update at the given position, if {@code world}
     * is an instance of {@link ServerLevel}
     *
     * @param world The target world
     * @param pos   The target position
     */
    public static void updateIfOnServer(Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel serverWorld)) return;
        serverWorld.getChunkSource().markForUpdate(pos);
    }

    /**
     * Same as {@link WorldOps#teleportToWorld(ServerPlayer, ServerLevel, Vec3, float, float)} but defaults
     * to {@code 0} for {@code pitch} and {@code yaw}
     */
    public static void teleportToWorld(ServerPlayer player, ServerLevel target, Vec3 pos) {
        teleportToWorld(player, target, pos, 0, 0);
    }

    /**
     * Teleports the given player to the given world, syncing all the annoying data
     * like experience and status effects that minecraft doesn't
     *
     * @param player The player to teleport
     * @param target The world to teleport to
     * @param pos    The target position
     * @param yaw    The target yaw
     * @param pitch  The target pitch
     */
    public static void teleportToWorld(ServerPlayer player, ServerLevel target, Vec3 pos, float yaw, float pitch) {
        player.teleportTo(target, pos.x, pos.y, pos.z, yaw, pitch);
        player.giveExperiencePoints(0);

        player.getActiveEffects().forEach(effect -> {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect, false));
        });
    }

}
