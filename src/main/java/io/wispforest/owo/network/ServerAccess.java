package io.wispforest.owo.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.function.Supplier;

public record ServerAccess(OwoNetChannel channel, ServerPlayer player, PacketSender responseSender) implements
    CommonAccess<ServerPlayer, MinecraftServer, ServerGamePacketListenerImpl> {

    @Override
    public MinecraftServer runtime() {
        return player.level().getServer();
    }

    @Override
    public ServerGamePacketListenerImpl packetListener() {
        return player.connection;
    }

    @Override
    public OwoNetChannel.CommonHandle responseHandle() {
        return channel.serverHandle(player);
    }
}
