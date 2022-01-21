package io.wispforest.owo.particles.system;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public interface ParticleHandler<T> {
    void executeParticleSystem(ClientWorld world, Vec3d pos, T data);
}
