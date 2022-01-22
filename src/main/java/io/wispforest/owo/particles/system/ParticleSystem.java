package io.wispforest.owo.particles.system;

import io.wispforest.owo.network.serialization.PacketBufSerializer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleSystem<T> {

    private final ParticleSystemManager manager;

    final int index;
    final PacketBufSerializer<T> adapter;
    final ParticleHandler<T> handler;

    ParticleSystem(ParticleSystemManager manager, Class<T> dataClass, int index, PacketBufSerializer<T> adapter, ParticleHandler<T> handler) {
        this.manager = manager;
        this.index = index;
        this.adapter = adapter;
        this.handler = handler;
    }

    public void execute(World world, Vec3d pos, T data) {
        if (world.isClient) {
            handler.executeParticleSystem(world, pos, data);
        } else {
            manager.sendPacket(this, (ServerWorld) world, pos, data);
        }
    }
}
