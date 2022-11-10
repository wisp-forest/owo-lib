package io.wispforest.owo.mixin;

import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ConfigSynchronizer.ServerPlayerEntityExtension {

    @Unique
    private final Map<String, Map<Option.Key, Object>> owo$optionStorage = new HashMap<>();

    @Override
    public Map<String, Map<Option.Key, Object>> owo$optionStorage() {
        return this.owo$optionStorage;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onScreenHandlerOpened", at = @At("HEAD"))
    private void attachScreenHandler(ScreenHandler screenHandler, CallbackInfo ci) {
        ((OwoScreenHandlerExtension) screenHandler).owo$attachToPlayer((ServerPlayerEntity) (Object) this);
    }
}
