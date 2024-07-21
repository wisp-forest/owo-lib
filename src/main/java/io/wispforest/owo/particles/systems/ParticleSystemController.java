package io.wispforest.owo.particles.systems;

import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.network.NetworkException;
import io.wispforest.owo.network.OwoHandshake;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * A controller object that manages and creates {@link ParticleSystem}s.
 * It is recommended to have one of these per mod.
 * <p>
 * To obtain a new particle system, call {@link #register(Class, Endec, ParticleSystemExecutor)}
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
    private final CustomPayload.Id<ParticleSystemPayload> payloadId;
    private int maxIndex = 0;
    private final String ownerClassName;

    private final ReflectiveEndecBuilder builder;

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
        OwoFreezer.checkRegister("Particle system controllers");

        this.builder = MinecraftEndecs.addDefaults(new ReflectiveEndecBuilder());

        if (REGISTERED_CONTROLLERS.containsKey(channelId)) {
            throw new IllegalStateException("Controller with id '" + channelId + "' was already registered from class '" +
                    REGISTERED_CONTROLLERS.get(channelId).ownerClassName + "'");
        }

        this.channelId = channelId;
        this.payloadId = new CustomPayload.Id<>(channelId);
        this.ownerClassName = ReflectionUtils.getCallingClassName(2);

        var instanceEndec = Endec.<ParticleSystemInstance<?>, Integer>dispatched(
            index -> {
                @SuppressWarnings("unchecked")
                var system = (ParticleSystem<Object>) systemsByIndex.get(index);
                return system.endec.xmap(x -> new ParticleSystemInstance<>(system, x), x -> x.data);
            },
            instance -> instance.system.index,
            Endec.VAR_INT
        );
        var endec = StructEndecBuilder.of(
            MinecraftEndecs.VEC3D.fieldOf("pos", ParticleSystemPayload::pos),
            instanceEndec.fieldOf("instance", ParticleSystemPayload::instance),
            (pos, instance) -> new ParticleSystemPayload(payloadId, pos, instance)
        );

        PayloadTypeRegistry.playS2C().register(payloadId, CodecUtils.toPacketCodec(endec));

        OwoHandshake.enable();
        OwoHandshake.requireHandshake();

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(payloadId, new Client()::handler);
        }

        REGISTERED_CONTROLLERS.put(channelId, this);
    }

    public ReflectiveEndecBuilder endecBuilder() {
        return this.builder;
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
    public <T> ParticleSystem<T> register(Class<T> dataClass, Endec<T> endec, ParticleSystemExecutor<T> executor) {
        int index = maxIndex++;
        var system = new ParticleSystem<>(this, dataClass, index, endec, executor);
        systemsByIndex.put(index, system);
        return system;
    }

    /**
     * Shorthand for {{@link #register(Class, Endec, ParticleSystemExecutor)}} which creates the endec
     * through {@link ReflectiveEndecBuilder#get(Class)}
     */
    public <T> ParticleSystem<T> register(Class<T> dataClass, ParticleSystemExecutor<T> executor) {
        return this.register(dataClass, this.builder.get(dataClass), executor);
    }

    /**
     * Registers the given system executor with the given
     * context data class, thereby creating a new system
     * <p>
     * This method defers executor registration, so
     * you must register the handler later in a client entrypoint.
     *
     * @param dataClass The class to use as context data
     * @param <T>       The type of context data to use
     * @return The created particle system
     * @see ParticleSystem#setHandler(ParticleSystemExecutor)
     */
    public <T> ParticleSystem<T> registerDeferred(Class<T> dataClass, Endec<T> endec) {
        int index = maxIndex++;
        var system = new ParticleSystem<>(this, dataClass, index, endec, null);
        systemsByIndex.put(index, system);
        return system;
    }

    /**
     * Shorthand for {{@link #registerDeferred(Class, Endec)}} which creates the endec
     * through {@link ReflectiveEndecBuilder#get(Class)}
     */
    public <T> ParticleSystem<T> registerDeferred(Class<T> dataClass) {
        return this.registerDeferred(dataClass, this.builder.get(dataClass));
    }

    <T> void sendPacket(ParticleSystem<T> particleSystem, ServerWorld world, Vec3d pos, T data) {
        ParticleSystemPayload payload = new ParticleSystemPayload(payloadId, pos, new ParticleSystemInstance<>(particleSystem, data));

        for (var player : PlayerLookup.tracking(world, BlockPos.ofFloored(pos))) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private void verify() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            for (ParticleSystem<?> system : systemsByIndex.values()) {
                if (system.handler == null) {
                    throw new NetworkException("Some particle systems of " + channelId + " don't have handlers registered");
                }
            }
        }
    }

    static {
        OwoFreezer.registerFreezeCallback(() -> {
            for (ParticleSystemController controller : REGISTERED_CONTROLLERS.values()) {
                controller.verify();
            }
        });
    }

    private record ParticleSystemInstance<T>(ParticleSystem<T> system, T data) {
        public void execute(World world, Vec3d pos) {
            system.handler.executeParticleSystem(world, pos, data);
        }
    }

    private record ParticleSystemPayload(CustomPayload.Id<ParticleSystemPayload> id, Vec3d pos, ParticleSystemInstance<?> instance) implements CustomPayload {
        @Override
        public Id<? extends CustomPayload> getId() {
            return id;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class Client {
        private void handler(ParticleSystemPayload payload, ClientPlayNetworking.Context context) {
            payload.instance.execute(context.client().world, payload.pos);
        }
    }
}