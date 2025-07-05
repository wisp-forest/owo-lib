package io.wispforest.owo.mixin.ffapi;

import net.minecraft.network.NetworkPhase;
import net.minecraft.util.Identifier;
import org.sinytra.fabric.networking_api.NeoNetworkRegistrar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(NeoNetworkRegistrar.class)
public abstract class NeoNetworkRegistrarMixin {

    @Shadow @Final @Mutable
    private Map<Identifier, NeoNetworkRegistrar.NeoPayloadHandler<?>> registeredPayloads;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void owo$adjustMapType(NetworkPhase protocol, CallbackInfo ci) {
        this.registeredPayloads = new ConcurrentHashMap<>(registeredPayloads);
    }
}
