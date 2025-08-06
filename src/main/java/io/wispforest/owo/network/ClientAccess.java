package io.wispforest.owo.network;

import io.wispforest.owo.mixin.neoforge.ClientAccessMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

///
/// This given class in neoforge will be modified with a client only self mixin as here [ClientAccessMixin] meaning such
/// will only allow for access in client environments.
///
public class ClientAccess implements OwoNetChannel.EnvironmentAccess<ClientPlayerEntity, MinecraftClient, ClientPlayNetworkHandler> {

    //@OnlyIn(Dist.CLIENT) private final ClientPlayNetworkHandler netHandler;
    //@OnlyIn(Dist.CLIENT) private final MinecraftClient instance = MinecraftClient.getInstance();

    public ClientAccess(ClientPlayNetworkHandler netHandler) {
        //this.netHandler = netHandler;
    }

    @Override
    //@OnlyIn(Dist.CLIENT)
    public ClientPlayerEntity player() {
        throw new IllegalStateException("Unable to get player as such has not been permitted for Server Env");
    }

    @Override
    //@OnlyIn(Dist.CLIENT)
    public MinecraftClient runtime() {
        throw new IllegalStateException("Unable to get runtime as such has not been permitted for Server Env");
    }

    @Override
    //@OnlyIn(Dist.CLIENT)
    public ClientPlayNetworkHandler netHandler() {
        throw new IllegalStateException("Unable to get netHandler as such has not been permitted for Server Env");
    }
}
