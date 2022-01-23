package io.wispforest.owo.mixin.tweaks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Mixin(Util.OperatingSystem.class)
public abstract class OperatingSystemMixin {

    @Shadow
    public abstract void open(URL url);

    @Inject(method = "open(Ljava/net/URL;)V", at = @At("HEAD"), cancellable = true)
    private void redirectOpenToWorker(URL url, CallbackInfo ci) {
        if (MinecraftClient.getInstance().isOnThread()) {
            CompletableFuture.runAsync(() -> this.open(url), Util.getMainWorkerExecutor());
            ci.cancel();
        }
    }
}
