package io.wispforest.owo.mixin;

import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onScreenHandlerOpened", at = @At("HEAD"))
    private void attachScreenHandler(ScreenHandler screenHandler, CallbackInfo ci) {
        ((OwoScreenHandlerExtension) screenHandler).owo$attachToPlayer((ServerPlayerEntity) (Object) this);
    }
}
