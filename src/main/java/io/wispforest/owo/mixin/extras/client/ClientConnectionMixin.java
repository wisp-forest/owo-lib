package io.wispforest.owo.mixin.extras.client;

import io.netty.channel.ChannelHandlerContext;
import io.wispforest.owo.extras.ClientConfigurationConnectionEvents;
import io.wispforest.owo.extras.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Shadow
    private PacketListener packetListener;

    @Inject(method = "channelInactive", at = @At("HEAD"))
    private void owo$disconnectAddon(ChannelHandlerContext channelHandlerContext, CallbackInfo ci) {
        if(packetListener instanceof ClientConfigurationNetworkHandler clientConfigurationNetworkHandler) {
            ClientConfigurationConnectionEvents.DISCONNECT.invoker().onConfigurationDisconnect(clientConfigurationNetworkHandler, MinecraftClient.getInstance());
        } else if(packetListener instanceof ClientPlayNetworkHandler clientPlayNetworkHandler) {
            ClientPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(clientPlayNetworkHandler, MinecraftClient.getInstance());
        }
    }

    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/listener/PacketListener;onDisconnected(Lnet/minecraft/network/DisconnectionInfo;)V"))
    private void owo$disconnectAddon(CallbackInfo ci) {
        if(packetListener instanceof ClientConfigurationNetworkHandler clientConfigurationNetworkHandler) {
            ClientConfigurationConnectionEvents.DISCONNECT.invoker().onConfigurationDisconnect(clientConfigurationNetworkHandler, MinecraftClient.getInstance());
        } else if(packetListener instanceof ClientPlayNetworkHandler clientPlayNetworkHandler) {
            ClientPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(clientPlayNetworkHandler, MinecraftClient.getInstance());
        }
    }
}
