package com.glisco.owo.ops;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A collection of common operations(s) done on {@link World}
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

    public static void playSound(World world, BlockPos pos, SoundEvent sound, SoundCategory category) {
        world.playSound(null, pos, sound, category, 1, 1);
    }

    public static void playSound(World world, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        world.playSound(null, pos, sound, category, volume, pitch);
    }

}
