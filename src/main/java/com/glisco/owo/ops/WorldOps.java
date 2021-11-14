package com.glisco.owo.ops;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A collection of common operations done on {@link World}
 */
public class WorldOps {

    /**
     * Breaks the specified block with the given item
     *
     * @param world     The world the block is in
     * @param pos       The position of the block to break
     * @param breakItem The item to break the block with
     */
    public static void breakBlockWithItem(World world, BlockPos pos, ItemStack breakItem) {
        BlockEntity breakEntity = world.getBlockState(pos).getBlock() instanceof BlockEntityProvider ? world.getBlockEntity(pos) : null;
        Block.dropStacks(world.getBlockState(pos), world, pos, breakEntity, null, breakItem);
        world.breakBlock(pos, false, null);
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
    public static void playSound(World world, Vec3d pos, SoundEvent sound, SoundCategory category) {
        playSound(world, new BlockPos(pos), sound, category, 1, 1);
    }

    public static void playSound(World world, BlockPos pos, SoundEvent sound, SoundCategory category) {
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
    public static void playSound(World world, Vec3d pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        world.playSound(null, new BlockPos(pos), sound, category, volume, pitch);
    }

    public static void playSound(World world, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        world.playSound(null, pos, sound, category, volume, pitch);
    }

    /**
     * Same as {@link WorldOps#teleportToWorld(ServerPlayerEntity, ServerWorld, Vec3d, float, float)} but defaults
     * to {@code 0} for {@code pitch} and {@code yaw}
     */
    public static void teleportToWorld(ServerPlayerEntity player, ServerWorld target, Vec3d pos) {
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
    public static void teleportToWorld(ServerPlayerEntity player, ServerWorld target, Vec3d pos, float yaw, float pitch) {
        player.teleport(target, pos.x, pos.y, pos.z, pitch, yaw);
        player.addExperience(0);

        player.getStatusEffects().forEach(effect -> {
            player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), effect));
        });
    }

}
