package io.wispforest.owo.mixin;

import io.wispforest.owo.util.OwoFreezer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 0)
public class MinecraftClientMixin {

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Group(name = "clientFreezeHooks", min = 1, max = 1)
    @Inject(method = "<init>", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startClient(Ljava/io/File;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void afterFabricHook(GameConfig args, CallbackInfo ci) {
        OwoFreezer.freeze();
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Group(name = "clientFreezeHooks", min = 1, max = 1)
    @Inject(method = "<init>", at = @At(value = "INVOKE", remap = false,
            target = "Lorg/quiltmc/loader/impl/game/minecraft/Hooks;startClient(Ljava/io/File;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void afterQuiltHook(GameConfig args, CallbackInfo ci) {
        OwoFreezer.freeze();
    }

}

