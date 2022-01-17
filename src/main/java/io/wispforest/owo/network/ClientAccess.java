package io.wispforest.owo.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;

public class ClientAccess implements OwoNetChannel.EnvironmentAccess<ClientPlayerEntity, MinecraftClient, ClientPlayNetworkHandler> {

    @Environment(EnvType.CLIENT) private final ClientPlayNetworkHandler netHandler;
    @Environment(EnvType.CLIENT) private final MinecraftClient instance = MinecraftClient.getInstance();

    public ClientAccess(ClientPlayNetworkHandler netHandler) {
        this.netHandler = netHandler;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ClientPlayerEntity player() {
        return instance.player;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public MinecraftClient runtime() {
        return instance;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ClientPlayNetworkHandler netHandler() {
        return netHandler;
    }
}
