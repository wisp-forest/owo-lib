package io.wispforest.owo.particles.systems;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface ParticleSystemExecutor<T> {
    /**
     * Called when particles should be displayed
     * at the given position in the given level,
     * with the given data as additional context
     *
     * @param level The level to display in
     * @param pos   The position to display at
     * @param data  The data to display with
     */
    void executeParticleSystem(Level level, Vec3 pos, T data);
}
