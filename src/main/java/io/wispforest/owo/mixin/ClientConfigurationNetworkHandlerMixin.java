package io.wispforest.owo.mixin;

import io.wispforest.owo.network.OwoClientConnectionExtension;
import io.wispforest.owo.network.QueuedChannelSet;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public class ClientConfigurationNetworkHandlerMixin {

    @ModifyArg(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/network/Connection;Lnet/minecraft/client/multiplayer/CommonListenerCookie;)V"))
    private Connection applyChannelSet(Connection connection) {
        ((OwoClientConnectionExtension) connection).owo$setChannelSet(QueuedChannelSet.channels);
        QueuedChannelSet.channels = null;

        return connection;
    }
}
