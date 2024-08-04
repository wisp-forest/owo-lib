package io.wispforest.owo.particles.systems;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
    void executeParticleSystem(Level world, Vec3 pos, T data);
}
