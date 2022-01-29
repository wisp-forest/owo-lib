package io.wispforest.owo.mixin;

import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
@SuppressWarnings("deprecation")
public class MainMixin {

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(method = "main", at = @At(value = "INVOKE",
            target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startServer(Ljava/io/File;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private static void afterFabricHook(CallbackInfo ci) {
        OwoNetChannel.freezeAllChannels();
    }

}
