package io.wispforest.owo.mixin.neoforge;

import io.netty.channel.ChannelHandlerContext;
import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.network.OwoHandshake;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionDisconnectHookMixin {

    @Shadow
    private PacketListener packetListener;

    @Inject(method = "channelInactive", at = @At("HEAD"))
    private void owoNeo$disconnectAddon(ChannelHandlerContext channelHandlerContext, CallbackInfo ci) {
        if(packetListener instanceof ClientConfigurationNetworkHandler) {
            OwoHandshake.onDisconnect();
        } else if (packetListener instanceof ClientPacketListener) {
            OwoHandshake.onDisconnect();
            ConfigSynchronizer.onDisconnect();
        }
    }

    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/listener/PacketListener;onDisconnected(Lnet/minecraft/network/DisconnectionInfo;)V"))
    private void owoNeo$disconnectAddon(CallbackInfo ci) {
        if(packetListener instanceof ClientConfigurationNetworkHandler) {
            OwoHandshake.onDisconnect();
        } else if (packetListener instanceof ClientPacketListener) {
            OwoHandshake.onDisconnect();
            ConfigSynchronizer.onDisconnect();
        }
    }
}
