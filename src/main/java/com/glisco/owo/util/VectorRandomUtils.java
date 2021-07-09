package com.glisco.owo.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class VectorRandomUtils {

    public static Vec3d getRandomCenteredOnBlock(World world, BlockPos pos, double deviation) {
        return getRandomOffset(world, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), deviation);
    }

    public static Vec3d getRandomWithinBlock(World world, BlockPos pos) {
        return getRandomOffset(world, Vec3d.of(pos).add(0.5, 0.5, 0.5), 0.5);
    }

    public static Vec3d getRandomOffset(World world, Vec3d center, double deviation) {
        return getRandomOffsetSpecific(world, center, deviation, deviation, deviation);
    }

    public static Vec3d getRandomOffsetSpecific(World world, Vec3d center, double deviationX, double deviationY, double deviationZ) {

        Random r = world.getRandom();

        double x = center.getX() + (r.nextDouble() - 0.5) * deviationX;
        double y = center.getY() + (r.nextDouble() - 0.5) * deviationY;
        double z = center.getZ() + (r.nextDouble() - 0.5) * deviationZ;

        return new Vec3d(x, y, z);
    }

}
