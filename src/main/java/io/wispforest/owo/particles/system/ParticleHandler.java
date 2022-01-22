package io.wispforest.owo.particles.system;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleHandler<T> {
    void executeParticleSystem(World world, Vec3d pos, T data);
}
