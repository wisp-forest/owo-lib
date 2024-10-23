package io.wispforest.owo.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClientAccess implements OwoNetChannel.EnvironmentAccess<ClientPlayerEntity, MinecraftClient, ClientPlayNetworkHandler> {

    @OnlyIn(Dist.CLIENT) private final ClientPlayNetworkHandler netHandler;
    @OnlyIn(Dist.CLIENT) private final MinecraftClient instance = MinecraftClient.getInstance();

    public ClientAccess(ClientPlayNetworkHandler netHandler) {
        this.netHandler = netHandler;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ClientPlayerEntity player() {
        return instance.player;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MinecraftClient runtime() {
        return instance;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ClientPlayNetworkHandler netHandler() {
        return netHandler;
    }
}
