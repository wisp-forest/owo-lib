package io.wispforest.owo.mixin;

import io.wispforest.owo.network.OwoClientConnectionExtension;
import io.wispforest.owo.network.OwoHandshake;
import io.wispforest.owo.network.QueuedChannelSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationNetworkHandler.class)
public abstract class ClientConfigurationNetworkHandlerMixin extends ClientCommonNetworkHandler {

    protected ClientConfigurationNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @ModifyArg(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/client/network/ClientConnectionState;)V"))
    private ClientConnection applyChannelSet(ClientConnection connection) {
        ((OwoClientConnectionExtension) connection).owo$setChannelSet(QueuedChannelSet.channels);
        QueuedChannelSet.channels = null;

        return connection;
    }

    //--

    @Inject(method = "onReady", at = @At(value = "NEW", target = "(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/client/network/ClientConnectionState;)Lnet/minecraft/client/network/ClientPlayNetworkHandler;"))
    public void owoNeo$handleComplete(ReadyS2CPacket packet, CallbackInfo ci) {
        OwoHandshake.handleReadyClient((ClientConfigurationNetworkHandler) (Object) this, this.client);
    }

}
