package io.wispforest.owo.mixin.neoforge;

import io.wispforest.owo.network.OwoHandshake;
import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(OwoNetChannel.class)
public abstract class OwoNetChannelClientMixin {
    @Shadow
    @Final
    private boolean required;

    @Shadow
    protected static Set<Identifier> getChannelSet(ClientConnection connection) {
        return null;
    }

    @Shadow
    @Final
    private CustomPayload.Id<OwoNetChannel.MessagePayload> packetId;

    @Inject(method = "canSendToServer", at = @At("HEAD"), cancellable = true)
    private void checkIfCanSend(CallbackInfoReturnable<Boolean> cir) {
        if (required) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(
                OwoHandshake.isValidClient() ?
                    getChannelSet(MinecraftClient.getInstance().getNetworkHandler().getConnection()).contains(this.packetId.id())
                    : NetworkRegistry.hasChannel(MinecraftClient.getInstance().getNetworkHandler(), this.packetId.id())
            );
        }
    }
}
