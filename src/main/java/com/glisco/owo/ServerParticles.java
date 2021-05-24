package com.glisco.owo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;

public class ServerParticles {

    private static HashMap<Identifier, ParticlePacketHandler> handlerRegistry = new HashMap<>();

    /**
     * Registers a handler that can react to particle events sent by the server
     *
     * @param id      The handler's id, <b>must be unique</b>
     * @param handler The handler itself
     */
    public static void registerClientSideHandler(Identifier id, ParticlePacketHandler handler) {
        if (handlerRegistry.containsKey(id)) throw new IllegalStateException("A handler with id " + id + " already exists");
        handlerRegistry.put(id, handler);
    }

    /**
     * Issues a particle event for the corresponding handler on all clients in range
     *
     * @param world         The world the event is happening in
     * @param pos           The position the event should happen at
     * @param handlerId     The client-side handler for this event
     * @param dataProcessor Optional consumer to add data to the sent packet
     */
    public static void issueEvent(ServerWorld world, BlockPos pos, Identifier handlerId, Consumer<PacketByteBuf> dataProcessor) {
        world.getServer().getPlayerManager().sendToAround(null, pos.getX(), pos.getY(), pos.getZ(), 50, world.getRegistryKey(), ParticleSystemPacket.create(handlerId, pos, dataProcessor));
    }

    /**
     * Issues a particle event for the corresponding handler of the given player
     *
     * @param player        The world the event is happening in
     * @param pos           The position the event should happen at
     * @param handlerId     The client-side handler for this event
     * @param dataProcessor Optional consumer to add data to the sent packet
     */
    public static void issueEvent(ServerPlayerEntity player, BlockPos pos, Identifier handlerId, Consumer<PacketByteBuf> dataProcessor) {
        player.networkHandler.sendPacket(ParticleSystemPacket.create(handlerId, pos, dataProcessor));
    }

    @Nullable
    static ParticlePacketHandler getHandler(Identifier id) {
        return handlerRegistry.getOrDefault(id, null);
    }

    @FunctionalInterface
    public interface ParticlePacketHandler {
        void onPacket(MinecraftClient client, BlockPos pos, PacketByteBuf data);
    }

}
