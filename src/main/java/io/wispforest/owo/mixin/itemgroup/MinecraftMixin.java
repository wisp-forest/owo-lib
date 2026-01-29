package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.Owo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    protected abstract Thread getRunningThread();

    @Inject(method = "setScreen", at = @At(value = "HEAD"))
    private void preventOffThreadScreenSet(Screen screen, CallbackInfo ci) {
        if (Thread.currentThread() != this.getRunningThread()) {
            if (Owo.DEBUG) {
                throw new IllegalStateException("Unable to invoke setScreen for '" + screen.getClass().getName() + "' as it was called not from the main thread! Please use `execute` on `Minecraft` instance.");
            } else {
                Owo.LOGGER.error("Found setScreen for '{}' called off thread! Please use tell the developer to use `execute` on `Minecraft` instance to prevent issues.", screen.getClass().getName());
            }
        }

    }
}
