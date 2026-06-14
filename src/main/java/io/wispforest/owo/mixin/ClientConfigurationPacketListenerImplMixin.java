package io.wispforest.owo.mixin;

import io.wispforest.owo.network.OwoClientConnectionExtension;
import io.wispforest.owo.network.OwoHandshake;
import io.wispforest.owo.network.QueuedChannelSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public class ClientConfigurationPacketListenerImplMixin {

    @ModifyArg(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/network/Connection;Lnet/minecraft/client/multiplayer/CommonListenerCookie;)V"))
    private Connection applyChannelSet(Connection connection) {
        ((OwoClientConnectionExtension) connection).owo$setChannelSet(QueuedChannelSet.channels);
        QueuedChannelSet.channels = null;

        return connection;
    }

    @Inject(method = "handleConfigurationFinished", at = @At(value = "NEW", target = "(Lnet/minecraft/core/RegistryAccess;Z)Lnet/neoforged/neoforge/event/TagsUpdatedEvent$ClientPacketReceived;"))
    private void owo$runConfigEventExtras(ClientboundFinishConfigurationPacket packet, CallbackInfo ci) {
        OwoHandshake.handleReadyClient((ClientConfigurationPacketListenerImpl)(Object)this, Minecraft.getInstance());
    }
}
