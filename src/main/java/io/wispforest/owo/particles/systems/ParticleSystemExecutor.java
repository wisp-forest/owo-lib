package io.wispforest.owo.particles.systems;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleSystemExecutor<T> {
    /**
     * Called when particles should be displayed
     * at the given position in the given world,
     * with the given data as additional context
     *
     * @param world The world to display in
     * @param pos   The position to display at
     * @param data  The data to display with
     */
    void executeParticleSystem(World world, Vec3d pos, T data);
}
