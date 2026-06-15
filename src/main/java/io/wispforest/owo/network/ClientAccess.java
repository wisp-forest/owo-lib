package io.wispforest.owo.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

public class ClientAccess implements OwoNetChannel.EnvironmentAccess<LocalPlayer, Minecraft, ClientPacketListener> {

    private final OwoNetChannel channel;
    @Environment(EnvType.CLIENT) private final ClientPacketListener packetListener;
    @Environment(EnvType.CLIENT) private final Minecraft instance = Minecraft.getInstance();

    public ClientAccess(OwoNetChannel channel, ClientPacketListener packetListener) {
        this.channel = channel;
        this.packetListener = packetListener;
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
}
