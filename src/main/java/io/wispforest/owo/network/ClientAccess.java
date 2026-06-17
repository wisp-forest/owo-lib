package io.wispforest.owo.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

public class ClientAccess implements CommonAccess<LocalPlayer, Minecraft, ClientPacketListener> {

    private final OwoNetChannel channel;
    private final PacketSender sender;
    @Environment(EnvType.CLIENT) private final ClientPacketListener packetListener;
    @Environment(EnvType.CLIENT) private final Minecraft instance = Minecraft.getInstance();

    public ClientAccess(OwoNetChannel channel, ClientPacketListener packetListener, PacketSender sender) {
        this.channel = channel;
        this.packetListener = packetListener;
        this.sender = sender;
    }

    @Override
    public OwoNetChannel channel() {
        return this.channel;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public LocalPlayer player() {
        return instance.player;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Minecraft runtime() {
        return instance;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ClientPacketListener packetListener() {
        return packetListener;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public OwoNetChannel.CommonHandle responseHandle() {
        return channel.clientHandle();
    }

    @Override
    public PacketSender responseSender() {
        return sender;
    }
}
