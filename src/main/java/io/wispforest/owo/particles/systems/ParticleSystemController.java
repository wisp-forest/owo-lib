package io.wispforest.owo.particles.systems;

import io.wispforest.owo.Owo;
import io.wispforest.owo.network.OwoHandshake;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.util.ReflectionUtils;
import io.wispforest.owo.util.VectorSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * A controller object that manages and creates {@link ParticleSystem}s.
 * It is recommended to have one of these per mod.
 * <p>
 * To obtain a new particle system, call {@link #register(Class, ParticleSystemExecutor)}
 * with the system's context data class and handler function. <b>It is important
 * that this is done on both client and server, otherwise joining the server
 * will fail in a handshake error</b>
 */
public class ParticleSystemController {

    @ApiStatus.Internal
    public static final Map<Identifier, ParticleSystemController> REGISTERED_CONTROLLERS = new HashMap<>();

    @ApiStatus.Internal
    public final Int2ObjectMap<ParticleSystem<?>> systemsByIndex = new Int2ObjectOpenHashMap<>();

    public final Identifier channelId;
    private int maxIndex = 0;
    private final String ownerClassName;

    /**
     * Creates a new controller with the given ID. Duplicate controller IDs
     * are not allowed - if there is a collision, the name of the
     * class that previously registered the controller will be part of
     * the exception. <b>This may be called at any stage during
     * mod initialization</b>
     *
     * @param channelId The packet ID to use
     */
    public ParticleSystemController(Identifier channelId) {
        if (REGISTERED_CONTROLLERS.containsKey(channelId)) {
            throw new IllegalStateException("Controller with id '" + channelId + "' was already registered from class '" +
                    REGISTERED_CONTROLLERS.get(channelId).ownerClassName + "'");
        }

        this.channelId = channelId;
        this.ownerClassName = ReflectionUtils.getCallingClassName(2);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(channelId, new Client()::handler);
        }

        REGISTERED_CONTROLLERS.put(channelId, this);
    }

    /**
     * Registers the given system executor with the given
     * context data class, thereby creating a new system
     *
     * @param dataClass The class to use as context data
     * @param executor  The code that is run to actually display the particle system
     * @param <T>       The type of context data to use
     * @return The created particle system
     */
    public <T> ParticleSystem<T> register(Class<T> dataClass, ParticleSystemExecutor<T> executor) {
        int index = maxIndex++;
        var system = new ParticleSystem<>(this, dataClass, index, PacketBufSerializer.get(dataClass), executor);
        systemsByIndex.put(index, system);
        return system;
    }

    <T> void sendPacket(ParticleSystem<T> particleSystem, ServerWorld world, Vec3d pos, T data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(particleSystem.index);
        VectorSerializer.write(pos, buf);
        particleSystem.adapter.serializer().accept(buf, data);

        for (var player : PlayerLookup.tracking(world, new BlockPos(pos))) {
            ServerPlayNetworking.send(player, channelId, buf);
        }
    }

    static {
        OwoHandshake.enable();
        OwoHandshake.requireHandshake();
    }

    @Environment(EnvType.CLIENT)
    private class Client {
        @SuppressWarnings("unchecked")
        private void handler(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
            int index = buf.readVarInt();

            Vec3d pos = VectorSerializer.read(buf);

            if (maxIndex <= index || index < 0) {
                Owo.LOGGER.warn("Received unknown particle system index {} on channel {}", index, channelId);
                return;
            }

            ParticleSystem<Object> system = (ParticleSystem<Object>) systemsByIndex.get(index);
            var data = system.adapter.deserializer().apply(buf);
            client.execute(() -> system.handler.executeParticleSystem(client.world, pos, data));
        }
    }
}
