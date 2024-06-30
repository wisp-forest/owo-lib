package io.wispforest.owo.mixin.extras.client;

import io.wispforest.owo.extras.ClientConfigurationConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientConfigurationNetworkHandler.class, priority = 999)
public abstract class ClientConfigurationNetworkHandlerMixin {

    @Inject(method = "onReady", at = @At(value = "NEW", target = "(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/client/network/ClientConnectionState;)Lnet/minecraft/client/network/ClientPlayNetworkHandler;"))
    public void owo$handleComplete(ReadyS2CPacket packet, CallbackInfo ci) {
        ClientConfigurationConnectionEvents.COMPLETE.invoker().onConfigurationComplete((ClientConfigurationNetworkHandler)(Object)this, MinecraftClient.getInstance());
    }
}
