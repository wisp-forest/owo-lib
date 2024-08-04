package io.wispforest.owo.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

public class ClientAccess implements OwoNetChannel.EnvironmentAccess<LocalPlayer, Minecraft, ClientPacketListener> {

    @Environment(EnvType.CLIENT) private final ClientPacketListener netHandler;
    @Environment(EnvType.CLIENT) private final Minecraft instance = Minecraft.getInstance();

    public ClientAccess(ClientPacketListener netHandler) {
        this.netHandler = netHandler;
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
    public ClientPacketListener netHandler() {
        return netHandler;
    }
}
