package io.wispforest.owo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Utility class for getting random offsets within a {@link Level}
 */
public final class VectorRandomUtils {

    private VectorRandomUtils() {}

    /**
     * Generates a random point centered on the given block
     *
     * @param level     The level to operate in
     * @param pos       The block position to take the center from
     * @param deviation The size of cube from which positions are picked
     * @return A random point no further than {@code deviation} from the center of {@code pos}
     */
    public static Vec3 getRandomCenteredOnBlock(Level level, BlockPos pos, double deviation) {
        return getRandomOffset(level, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), deviation);
    }

    /**
     * Generates a random point within the given block
     *
     * @param level The level to operate in
     * @param pos   The block in which to pick a point
     * @return A random point somewhere within the bounding box of {@code pos}
     */
    public static Vec3 getRandomWithinBlock(Level level, BlockPos pos) {
        return getRandomOffset(level, Vec3.atLowerCornerOf(pos).add(0.5, 0.5, 0.5), 0.5);
    }

    /**
     * Generates a random point
     *
     * @param level     The level to operate in
     * @param center    The center point
     * @param deviation The size of cube from which positions are picked
     * @return A random point within a cube with side length of {@code deviation} centered on {@code center}
     */
    public static Vec3 getRandomOffset(Level level, Vec3 center, double deviation) {
        return getRandomOffsetSpecific(level, center, deviation, deviation, deviation);
    }

    /**
     * Generates a random point offset from {@code center}
     *
     * @param level      The level to operate in
     * @param center     The center position to start with
     * @param deviationX The length of the selection cuboid on the x-axis
     * @param deviationY The length of the selection cuboid on the y-axis
     * @param deviationZ The length of the selection cuboid on the z-axis
     * @return The generated point
     */
    public static Vec3 getRandomOffsetSpecific(Level level, Vec3 center, double deviationX, double deviationY, double deviationZ) {

        final var r = level.getRandom();

        double x = center.x() + (r.nextDouble() - 0.5) * deviationX;
        double y = center.y() + (r.nextDouble() - 0.5) * deviationY;
        double z = center.z() + (r.nextDouble() - 0.5) * deviationZ;

        return new Vec3(x, y, z);
    }

}
