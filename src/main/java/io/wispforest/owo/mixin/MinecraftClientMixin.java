package io.wispforest.owo.mixin;

import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
@SuppressWarnings("deprecation")
public class MinecraftClientMixin {

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startClient(Ljava/io/File;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void afterFabricHook(RunArgs args, CallbackInfo ci) {
        OwoNetChannel.freezeAllChannels();
    }

}

