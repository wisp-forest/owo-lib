package io.wispforest.owo.particles.systems;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.NetworkException;
import io.wispforest.owo.network.OwoHandshake;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
    private final StructEndec<ParticleSystemPayload> payloadEndec;
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
        this.payloadEndec = StructEndecBuilder.of(
            MinecraftEndecs.VEC3.fieldOf("pos", ParticleSystemPayload::pos),
            instanceEndec.fieldOf("instance", ParticleSystemPayload::instance),
            (pos, instance) -> new ParticleSystemPayload(channelId, pos, instance)
        );

        OwoHandshake.enable();
        OwoHandshake.requireHandshake();

        REGISTERED_CONTROLLERS.put(channelId, this);
    }

    @ApiStatus.Internal
    public static void initNetworking() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Owo.MAIN.registerClientbound(ParticleSystemPayload.class, ParticleSystemPayload.ENDEC, new Client()::handler);
        } else {
            Owo.MAIN.registerClientboundDeferred(ParticleSystemPayload.class, ParticleSystemPayload.ENDEC);
        }
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

    <T> void sendPacket(ParticleSystem<T> particleSystem, ServerLevel level, Vec3 pos, T data) {
        ParticleSystemPayload payload = new ParticleSystemPayload(channelId, pos, new ParticleSystemInstance<>(particleSystem, data));

        Owo.MAIN.serverHandle(PlayerLookup.tracking(level, BlockPos.containing(pos))).send(payload);
    }

    private void verify() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
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
        public void execute(Level level, Vec3 pos) {
            system.handler.executeParticleSystem(level, pos, data);
        }
    }

    private record ParticleSystemPayload(Identifier id, Vec3 pos, ParticleSystemInstance<?> instance) {
        public static final StructEndec<ParticleSystemPayload> ENDEC = Endec.dispatchedStruct(
            id -> REGISTERED_CONTROLLERS.get(id).payloadEndec,
            ParticleSystemPayload::id,
            MinecraftEndecs.IDENTIFIER
        );
    }

    @Environment(EnvType.CLIENT)
    private static class Client {
        private void handler(ParticleSystemPayload payload, ClientAccess context) {
            payload.instance.execute(context.runtime().level, payload.pos);
        }
    }
}
