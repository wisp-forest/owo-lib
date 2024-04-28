package io.wispforest.owo.mixin;

import io.wispforest.owo.network.OwoClientConnectionExtension;
import io.wispforest.owo.network.OwoHandshake;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientConfigurationNetworkHandler.class)
public class ClientConfigurationNetworkHandlerMixin {

    @ModifyArg(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/client/network/ClientConnectionState;)V"))
    private ClientConnection applyChannelSet(ClientConnection connection) {
        ((OwoClientConnectionExtension) connection).owo$setChannelSet(OwoHandshake.clientChannelSet);
        OwoHandshake.clientChannelSet = null;

        return connection;
    }
}
