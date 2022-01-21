package io.wispforest.owo.particles.system;

import io.wispforest.owo.Owo;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.network.serialization.TypeAdapter;
import io.wispforest.owo.util.VectorSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.BiConsumer;

public class ParticleSystemManager {
    private final Identifier id;
    private final Int2ObjectMap<ParticleSystem<?>> indexToSystemMap = new Int2ObjectOpenHashMap<>();
    private int maxIndex = 0;

    public ParticleSystemManager(Identifier id) {
        this.id = id;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(id, this::clientHandler);
        }
    }

    public <T> ParticleSystem<T> register(Class<T> dataClass, ParticleHandler<T> handler) {
        int index = maxIndex++;
        var system = new ParticleSystem<>(this, dataClass, index, TypeAdapter.get(dataClass), handler);
        indexToSystemMap.put(index, system);
        return system;
    }

    @SuppressWarnings("unchecked")
    private void clientHandler(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
        int index = buf.readVarInt();

        Vec3d pos = VectorSerializer.read(buf);

        if (maxIndex <= index || index < 0) {
            Owo.LOGGER.warn("Received unknown particle system index {} on channel {}", index, id);
            return;
        }

        ParticleSystem<Object> system = (ParticleSystem<Object>) indexToSystemMap.get(index);
        Object data = system.adapter.deserializer().apply(buf);
        system.handler.executeParticleSystem(client.world, pos, data);
    }

    public <T> void sendPacket(ParticleSystem<T> particleSystem, ServerWorld world, Vec3d pos, T data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(particleSystem.index);
        VectorSerializer.write(pos, buf);
        particleSystem.adapter.serializer().accept(buf, data);

        for (var player : PlayerLookup.tracking(world, new BlockPos(pos))) {
            ServerPlayNetworking.send(player, id, buf);
        }
    }
}
