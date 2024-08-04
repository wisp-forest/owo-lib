package io.wispforest.owo.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ServerAccess(ServerPlayer player) implements
        OwoNetChannel.EnvironmentAccess<ServerPlayer, MinecraftServer, ServerGamePacketListenerImpl> {

    @Override
    public MinecraftServer runtime() {
        return player.server;
    }

    @Override
    public ServerGamePacketListenerImpl netHandler() {
        return player.connection;
    }
}
