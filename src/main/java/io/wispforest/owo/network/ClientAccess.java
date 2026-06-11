package io.wispforest.owo.network;

import io.wispforest.owo.neoforge.env.EnvType;
import io.wispforest.owo.neoforge.env.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

public class ClientAccess implements OwoNetChannel.EnvironmentAccess<LocalPlayer, Minecraft, ClientPacketListener> {

    @Environment(EnvType.CLIENT) private final ClientPacketListener packetListener;
    @Environment(EnvType.CLIENT) private final Minecraft instance = Minecraft.getInstance();

    public ClientAccess(ClientPacketListener packetListener) {
        this.packetListener = packetListener;
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
