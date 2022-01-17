package io.wispforest.owo.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public record ServerAccess(ServerPlayerEntity player) implements
        OwoNetChannel.EnvironmentAccess<ServerPlayerEntity, MinecraftServer, ServerPlayNetworkHandler> {

    @Override
    public MinecraftServer runtime() {
        return player.server;
    }

    @Override
    public ServerPlayNetworkHandler netHandler() {
        return player.networkHandler;
    }
}
