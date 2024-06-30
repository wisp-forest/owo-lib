package io.wispforest.owo.mixin.extras;

import io.wispforest.owo.extras.ServerPlayConnectionEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/PlayerAbilitiesS2CPacket;<init>(Lnet/minecraft/entity/player/PlayerAbilities;)V"))
    private void handlePlayerConnection(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData arg, CallbackInfo ci) {
        ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(player.networkHandler, player.networkHandler::send, player.server);}
}
