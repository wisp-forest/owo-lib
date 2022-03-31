package io.wispforest.owo.particles.systems;

import io.wispforest.owo.network.NetworkException;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.util.ServicesFrozenException;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a particle effect that can be played
 * at a position in a world <i>on both client and server</i>,
 * with some optional data attached.
 * <br>
 * To run this effect, call {@link #spawn(World, Vec3d, Object)}. If you call this
 * on the server, a command will be sent to the client to execute the system.
 * <b>Thus, it is important this is registered on both client and server</b>
 * <p>
 * In case your particle effect not required any additional data,
 * use {@link Void} as the data class and pass {@code null} to {@link #spawn(World, Vec3d, Object)}
 * <p>
 * The data is serialized with the {@link PacketBufSerializer} system,
 * so should your data class not be supported, register your own
 * serializer with {@link PacketBufSerializer#register(Class, BiConsumer, Function)}
 *
 * @param <T> The data class
 */
public class ParticleSystem<T> {

    private final ParticleSystemController manager;

    final Class<T> dataClass;
    final int index;
    final PacketBufSerializer<T> adapter;
    ParticleSystemExecutor<T> handler;

    private final boolean permitsContextlessExecution;

    ParticleSystem(ParticleSystemController manager, Class<T> dataClass, int index, PacketBufSerializer<T> adapter, ParticleSystemExecutor<T> handler) {
        OwoFreezer.checkRegister("Particle systems");

        this.manager = manager;
        this.dataClass = dataClass;
        this.index = index;
        this.adapter = adapter;
        this.handler = handler;

        this.permitsContextlessExecution = dataClass == Void.class;
    }

    /**
     * Sets the particle system's handler.
     *
     * @param handler the code that is run to actually display the particle system
     * @throws NetworkException if this particle system already has a handler
     */
    public void setHandler(ParticleSystemExecutor<T> handler) {
        if (OwoFreezer.isFrozen()) throw new ServicesFrozenException("Particle systems can only be changed during mod init");
        if (this.handler != null) throw new NetworkException("Particle system already has a handler");

        this.handler = handler;
    }

    /**
     * Spawns, or displays, whichever term you prefer,
     * this particle system in the given world at the
     * given position and with the passed context data
     *
     * <p><b>{@code null} data is only allowed if the data class of this
     * particle system is {@link Void}</b>
     *
     * @param world The world to execute in
     * @param pos   The position to execute at
     * @param data  The context to execute with
     */
    public void spawn(World world, Vec3d pos, @Nullable T data) {
        if (data == null && !permitsContextlessExecution) throw new IllegalStateException("This particle system does not permit 'null' data");

        if (world.isClient) {
            handler.executeParticleSystem(world, pos, data);
        } else {
            manager.sendPacket(this, (ServerWorld) world, pos, data);
        }
    }

    /**
     * Convenience wrapper for {@link #spawn(World, Vec3d, Object)}
     * that always passes {@code null} data
     *
     * @param world The world to execute in
     * @param pos   The position to execute at
     */
    public void spawn(World world, Vec3d pos) {
        spawn(world, pos, null);
    }
}
