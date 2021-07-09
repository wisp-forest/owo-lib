package com.glisco.owo.ops;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldOps {

    public static void breakBlockWithItem(World world, BlockPos pos, ItemStack breakItem) {
        BlockEntity breakEntity = world.getBlockState(pos).getBlock() instanceof BlockEntityProvider ? world.getBlockEntity(pos) : null;
        Block.dropStacks(world.getBlockState(pos), world, pos, breakEntity, null, breakItem);
        world.breakBlock(pos, false, null);
    }

}
