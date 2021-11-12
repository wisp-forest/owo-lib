package io.wispforest.owo.ops;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
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

    public static void updateIfOnServer(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        serverWorld.getChunkManager().markForUpdate(pos);
    }

}
