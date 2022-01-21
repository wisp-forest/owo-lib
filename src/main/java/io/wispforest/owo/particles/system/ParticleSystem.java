package io.wispforest.owo.particles.system;

import io.wispforest.owo.network.serialization.TypeAdapter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleSystem<T> {
    final ParticleSystemManager manager;
    final Class<T> dataClass;
    final int index;
    final TypeAdapter<T> adapter;
    final ParticleHandler<T> handler;

    ParticleSystem(ParticleSystemManager manager, Class<T> dataClass, int index, TypeAdapter<T> adapter, ParticleHandler<T> handler) {
        this.manager = manager;
        this.dataClass = dataClass;
        this.index = index;
        this.adapter = adapter;
        this.handler = handler;
    }

    public void execute(World world, Vec3d pos, T data) {
        if (world.isClient) {
            handler.executeParticleSystem((ClientWorld) world, pos, data);
        } else {
            manager.sendPacket(this, (ServerWorld)world, pos, data);
        }
    }
}
