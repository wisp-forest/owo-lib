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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A collection of common operations done on {@link Level}
 */
public final class LevelOps {

    private LevelOps() {}

    /**
     * Break the specified block with the given item
     *
     * @param level     The level the block is in
     * @param pos       The position of the block to break
     * @param breakItem The item to break the block with
     */
    public static void breakBlockWithItem(Level level, BlockPos pos, ItemStack breakItem) {
        breakBlockWithItem(level, pos, breakItem, null);
    }

    /**
     * Break the specified block with the given item
     *
     * @param level          The level the block is in
     * @param pos            The position of the block to break
     * @param breakItem      The item to break the block with
     * @param breakingEntity The entity which is breaking the block
     */
    public static void breakBlockWithItem(Level level, BlockPos pos, ItemStack breakItem, @Nullable Entity breakingEntity) {
        BlockEntity breakEntity = level.getBlockState(pos).getBlock() instanceof EntityBlock ? level.getBlockEntity(pos) : null;
        Block.dropResources(level.getBlockState(pos), level, pos, breakEntity, breakingEntity, breakItem);
        level.destroyBlock(pos, false, breakingEntity);
    }

    /**
     * Plays the provided sound at the provided location. This works on both client
     * and server. Volume and pitch default to 1
     *
     * @param level    The level to play the sound in
     * @param pos      Where to play the sound
     * @param sound    The sound to play
     * @param category The category for the sound
     */
    public static void playSound(Level level, Vec3 pos, SoundEvent sound, SoundSource category) {
        playSound(level, BlockPos.containing(pos), sound, category, 1, 1);
    }

    public static void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource category) {
        playSound(level, pos, sound, category, 1, 1);
    }

    /**
     * Plays the provided sound at the provided location. This works on both client
     * and server
     *
     * @param level    The level to play the sound in
     * @param pos      Where to play the sound
     * @param sound    The sound to play
     * @param category The category for the sound
     * @param volume   The volume to play the sound at
     * @param pitch    The pitch, or speed, to play the sound at
     */
    public static void playSound(Level level, Vec3 pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        level.playSound(null, BlockPos.containing(pos), sound, category, volume, pitch);
    }

    public static void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        level.playSound(null, pos, sound, category, volume, pitch);
    }

    /**
     * Causes a block update at the given position, if {@code level}
     * is an instance of {@link ServerLevel}
     *
     * @param level The target level
     * @param pos   The target position
     */
    public static void updateIfOnServer(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverWorld)) return;
        serverWorld.getChunkSource().blockChanged(pos);
    }

    /**
     * Same as {@link LevelOps#teleportToLevel(ServerPlayer, ServerLevel, Vec3, float, float)} but defaults
     * to {@code 0} for {@code pitch} and {@code yaw}
     */
    public static void teleportToLevel(ServerPlayer player, ServerLevel target, Vec3 pos) {
        teleportToLevel(player, target, pos, 0, 0);
    }

    /**
     * Teleports the given player to the given world, syncing all the annoying data
     * like experience and status effects that minecraft doesn't
     *
     * @param player The player to teleport
     * @param target The level to teleport to
     * @param pos    The target position
     * @param yaw    The target yaw
     * @param pitch  The target pitch
     */
    public static void teleportToLevel(ServerPlayer player, ServerLevel target, Vec3 pos, float yaw, float pitch) {
        player.teleportTo(target, pos.x, pos.y, pos.z, Set.of(), yaw, pitch, false);
        player.giveExperiencePoints(0);

        player.getActiveEffects().forEach(effect -> {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect, false));
        });
    }

}
