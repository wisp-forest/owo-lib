package io.wispforest.owo.mixin.neoforge;

import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientAccess.class)
public abstract class ClientAccessMixin implements OwoNetChannel.EnvironmentAccess<ClientPlayerEntity, MinecraftClient, ClientPlayNetworkHandler> {

    @Unique
    private ClientPlayNetworkHandler netHandler;

    @Unique
    private final MinecraftClient instance = MinecraftClient.getInstance();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void owo$setNetHandler(PlayerEntity player, CallbackInfo ci) {
        this.netHandler = ((ClientPlayerEntity) player).networkHandler;
    }

    @Override
    public ClientPlayerEntity player() {
        return instance.player;
    }

    @Override
    public MinecraftClient runtime() {
        return instance;
    }

    @Override
    public ClientPlayNetworkHandler netHandler() {
        return netHandler;
    }
}
